/* ==========================================
 * jORLib : a free Java OR library
 * ==========================================
 *
 * Project Info:  https://github.com/jkinable/jorlib
 * Project Creator:  Joris Kinable (https://github.com/jkinable)
 *
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * This program and the accompanying materials are licensed under GPLv3
 *
 */
/* -----------------
 * FinishGeneratingCutsEvent.java
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
package org.jorlib.frameworks.columnGeneration.branchAndPrice.EventHandling;

import org.jorlib.frameworks.columnGeneration.master.cutGeneration.Inequality;

import java.util.EventObject;
import java.util.List;

/**
 * Event generated when the cut handler finished separating violated inequalities
 *
 * @author Joris Kinable
 * @version 5-5-2015
 */
public class FinishGeneratingCutsEvent extends EventObject{

    public final List<Inequality> separatedInequalities;
    /**
     * Creates a new FinishGeneratingCutsEvent
     * @param source Generator of the event
     */
    public FinishGeneratingCutsEvent(Object source, List<Inequality> separatedInequalities){
        super(source);
        this.separatedInequalities=separatedInequalities;
    }
}
