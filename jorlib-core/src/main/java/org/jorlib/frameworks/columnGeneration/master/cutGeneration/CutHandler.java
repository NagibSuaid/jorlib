/* ==========================================
 * jORLib : a free Java OR library
 * ==========================================
 *
 * Project Info:  https://github.com/jkinable/jorlib
 * Project Creator:  Joris Kinable (https://github.com/jkinable)
 *
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * This program and the accompanying materials are licensed under LGPLv2.1
 *
 */
/* -----------------
 * CutHandler.java
 * -----------------
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jorlib.frameworks.columnGeneration.master.cutGeneration;

import java.util.*;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.CHListener;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.FinishGeneratingCutsEvent;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling.StartGeneratingCutsEvent;
import org.jorlib.frameworks.columnGeneration.master.MasterData;
import org.jorlib.frameworks.columnGeneration.model.ModelInterface;
import org.jorlib.frameworks.columnGeneration.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The CutHandler is a manager class which maintains various {@link AbstractCutGenerator}(s) to generate inequalities of different types.
 * 
 * @author Joris Kinable
 * @version 13-4-2015
 *
 */
public class CutHandler<T extends ModelInterface,W extends MasterData>{

	/** Logger for this class **/
	protected final Logger logger = LoggerFactory.getLogger(CutHandler.class);
	/** Configuration file for this class **/
	protected final Configuration config=Configuration.getConfiguration();

	/** Set of CutGenerators **/
	protected Set<AbstractCutGenerator<T,W>> cutGenerators;
	/** Helper class which notifies CHListeners **/
	CHNotifier notifier;

	/** Creates a new CutHandler **/
	public CutHandler(){
		cutGenerators=new LinkedHashSet<>();
		notifier=new CHNotifier();
	}
	
	
	/**
	 * Supply the data object containing the data from the master problem which is required by the cutGenerators to
	 * separate valid inequalities.
	 *
	 * @param masterData Master data object
	 */
	public void setMasterData(W masterData){
		for(AbstractCutGenerator<T,W> cg : cutGenerators){
			cg.setMasterData(masterData);
		}
	}
	
	/**
	 * Add a new cutGenerator
	 * @param cutGenerator cut generator
	 */
	public void addCutGenerator(AbstractCutGenerator<T,W> cutGenerator){
		cutGenerators.add(cutGenerator);
	}

	/**
	 * Remove a cutGenerator
	 * @param cutGenerator cut generator
	 */
	public void removeCutGenerator(AbstractCutGenerator<T,W> cutGenerator){
		cutGenerators.remove(cutGenerator);
	}
	
	/**
	 * Generates inequalities for the master problem of the column generation model, by invoking the {@link AbstractCutGenerator#generateInqualities() generateInequalities} method for each
	 * of the registered cut generators. Returns as soon as a single cut has been found!
	 * @return true if inequalities have been found
	 */
	public boolean generateInequalities(){
		List<AbstractInequality> separatedInequalities=new ArrayList<>();
		notifier.fireStartGeneratingCutsEvent();
		for(AbstractCutGenerator<T,W> cutGen: cutGenerators){
			separatedInequalities.addAll(cutGen.generateInqualities());
			if(config.QUICK_RETURN_AFTER_CUTS_FOUND && !separatedInequalities.isEmpty())
				break;
		}
		notifier.fireFinishGeneratingCutsEvent(separatedInequalities);
		return !separatedInequalities.isEmpty();
	}
	
	/**
	 * Add a set of inequalities from an external source. Can be used to add a number of initial inequalities which are not separated from the
	 * master problem.
	 * @param cuts Collection of inequalities
	 */
	public void addCuts(Collection<AbstractInequality> cuts){
		System.out.println("Cuthandler: Added initial inequalities: " + cuts.size());
		for(AbstractInequality cut : cuts){
			if(!this.cutGenerators.contains(cut.maintainingGenerator))
				throw new RuntimeException("Attempt to add cut failed. AbstractCutGenerator for this type of inequalities is not registered with the cut handler!");
			else
				cut.maintainingGenerator.addCut(cut);
		}
	}
	
	/**
	 * Gets a list of all inequalities generated by the Cut Generators
	 * @return A list of all inequalities generated by the Cut Generators
	 */
	public List<AbstractInequality> getCuts(){
		List<AbstractInequality> cuts=new ArrayList<>();
		for(AbstractCutGenerator<T,W> cutGen : cutGenerators)
			cuts.addAll(cutGen.getCuts());
		return cuts;
	}
	
	/**
	 * Close the Cut Generators
	 */
	public void close(){
		for(AbstractCutGenerator<T,W> cutGen : cutGenerators){
			cutGen.close();
		}
	}

	/**
	 * Adds a CHlistener
	 * @param listener listener
	 */
	public void addCHEventListener(CHListener listener) {
		notifier.addListener(listener);
	}

	/**
	 * Removes a CHlistener
	 * @param listener listener
	 */
	public void removeCHEventListener(CHListener listener) {
		notifier.removeListener(listener);
	}

	/**
	 * Inner Class which notifies CHListeners
	 */
	protected class CHNotifier {
		/**
		 * Listeners
		 */
		private Set<CHListener> listeners;

		/**
		 * Creates a new BAPNotifier
		 */
		public CHNotifier() {
			listeners = new LinkedHashSet<>();
		}

		/**
		 * Adds a listener
		 *
		 * @param listener listener
		 */
		public void addListener(CHListener listener) {
			this.listeners.add(listener);
		}

		/**
		 * Removes a listener
		 *
		 * @param listener listener
		 */
		public void removeListener(CHListener listener) {
			this.listeners.remove(listener);
		}

		/**
		 * Fires a StartGeneratingCutsEvent to indicate that the cut handler starts generating inequalities
		 */
		public void fireStartGeneratingCutsEvent() {
			StartGeneratingCutsEvent startGeneratingCutsEvent = null;
			for (CHListener listener : listeners) {
				if (startGeneratingCutsEvent == null)
					startGeneratingCutsEvent = new StartGeneratingCutsEvent(CutHandler.this);
				listener.startGeneratingCuts(startGeneratingCutsEvent);
			}
		}

		/**
		 * Fires a FinishGeneratingCutsEvent to indicate that that the cut handler finished generating inequalities
		 * @param separatedInequalities list of newly separated inequalities which have been generated
		 */
		public void fireFinishGeneratingCutsEvent(List<AbstractInequality> separatedInequalities) {
			FinishGeneratingCutsEvent finishGeneratingCutsEvent = null;
			for (CHListener listener : listeners) {
				if (finishGeneratingCutsEvent == null)
					finishGeneratingCutsEvent = new FinishGeneratingCutsEvent(CutHandler.this, Collections.unmodifiableList(separatedInequalities));
				listener.finishGeneratingCuts(finishGeneratingCutsEvent);
			}
		}
	}
}
