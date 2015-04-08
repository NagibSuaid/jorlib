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
 * Knapsack.java
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
package org.jorlib.alg;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Branch and bound implementation of knapsack. 
 * 
 * Solves the problem:
 * max \sum_i c_i x_i
 * s.t. \sum_i a_i x_i <= b
 * x_i binary
 * 
 * The implementation is space efficient: it does not rely on large matrices.
 * The knapsack problem is solved as a binary tree problem. Each level of the tree corresponds to a specific item. Each time we branch on a particular node, two child-nodes 
 * are created, reflecting whether the item at the level of the child nodes is selected, or not. As a result, at most 2^n nodes, where n is the number of items, are created.
 * In practice, the number of generated nodes is significantly smaller, as the number of items one can choose depends on the knapsack weight. Furthermore, the search tree is pruned using
 * bounds. 
 * Consider replacing this implementation by a faster one such as MT2
 * 
 * NOTE: All item weights, as well as the maxKnapsackWeight have to be integers. The item weights can be fractional, both positive and negative. Obviously, since this is a
 * maximization problem, items with a value smaller or equal to 0 are never selected.
 * 
 *
 * @author Joris Kinable
 * @since April 8, 2015
 */
public class Knapsack{
	
	/**
	 * Calculates a greedy solution for the knapsack problem. This solution is a valid lower bound and is used for pruning.
	 * @param nrItems Maximum number of items in the knapsack
	 * @param itemOrder Order in which the items are considered by the greedy algorithm. The items are sorted ascending, based on their value/weight ratio
	 * @param maxKnapsackWeight Max size of knapsack
	 * @param itemValues
	 * @param itemWeights
	 * @return
	 */
	private static KnapsackResult getGreedyKnapsackSolution(int nrItems, Integer[] itemOrder, int maxKnapsackWeight, double[] itemValues, int[] itemWeights){
		double value=0;
		int remainingWeight=maxKnapsackWeight;
		boolean[] selectedItems=new boolean[nrItems];
		//Greedily take a single item until the knapsack is full
		for(int i=0; i<nrItems; i++){
			if(itemWeights[itemOrder[i]]<=remainingWeight){
				value+=itemValues[itemOrder[i]];
				remainingWeight-=itemWeights[itemOrder[i]];
				selectedItems[itemOrder[i]]=true;
			}
		}
		return new KnapsackResult(selectedItems, maxKnapsackWeight-remainingWeight, value);
	}
	
	/**
	 * Sort the times in ascending order, based on their value/weigth ratio
	 */
	private static void sortItems(int nrItems, Integer[] itemOrder, double[] itemValues, int[] itemWeights){
		Arrays.sort(itemOrder, new Comparator<Integer>() {
			@Override
			public int compare(Integer item1, Integer item2) {
				return -1*Double.compare(itemValues[item1]/itemWeights[item1], itemValues[item2]/itemWeights[item2]);
			}
		});
	}
	
	/**
	 * Solve the knapsack problem.
	 * @param nrItems nr of items in the knapsack
	 * @param maxKnapsackWeight max size/weight of the knapsack
	 * @param itemValues
	 * @param itemWeights
	 * @return
	 */
	public static KnapsackResult runKnapsack(int nrItems, int maxKnapsackWeight, double[] itemValues, int[] itemWeights){
		Queue<KnapsackNode> queue=new PriorityQueue<KnapsackNode>();
		
		//Define the order in which items will be processed. The items are sorted based on their value/weight ratio, thereby considering proportionally more valuable items first.
		Integer[] itemOrder=new Integer[nrItems];
		for(int i=0; i<nrItems; i++) itemOrder[i]=i;
		sortItems(nrItems, itemOrder, itemValues, itemWeights);
		
		//TEMP
//		double[] ratios=new double[nrItems];
//		for(int i=0; i<nrItems; i++){
//			ratios[i]=itemValues[itemOrder[i]]/itemWeights[itemOrder[i]];
//		}
//		System.out.println("Order: "+Arrays.toString(itemOrder));
//		System.out.println("Ratios: "+Arrays.toString(ratios));
		//END TEMP
		
		//Create initial node
		KnapsackNode kn=new KnapsackNode(nrItems);
		kn.bound=calcBound(nrItems, itemOrder, itemValues, itemWeights, kn.level+1, maxKnapsackWeight-kn.weight, kn.value);
		queue.add(kn);
		
		//Get initial greedy solution
		KnapsackResult greedySolution=getGreedyKnapsackSolution(nrItems, itemOrder, maxKnapsackWeight, itemValues, itemWeights);
//		System.out.println("Init sol: "+greedySolution);
		
		//Maintain a reference to the best node
		double bestValue=greedySolution.value;
		KnapsackNode bestNode=null;
		
		while(!queue.isEmpty()){
			kn=queue.poll();
			if(kn.bound>bestValue && kn.level<nrItems-1){
				kn.level++;
				//Create 2 new nodes, one where item <itemToAdd> is used, and one where item <itemToAdd> is skipped.
				int itemToAdd=itemOrder[kn.level];
				if(kn.weight+itemWeights[itemToAdd]<=maxKnapsackWeight && itemValues[itemToAdd]>0){ //Check whether we can add the next item and whether its value is positive
					KnapsackNode knCopy=kn.copy();
					knCopy.addItem(itemToAdd, itemWeights[itemToAdd], itemValues[itemToAdd]);
					knCopy.bound=calcBound(nrItems, itemOrder, itemValues, itemWeights, knCopy.level+1, maxKnapsackWeight-knCopy.weight, knCopy.value);
					
					if(knCopy.value>bestValue){
						bestValue=knCopy.value;
						//this.selectedItems=knCopy.additionalStudents;
						bestNode=knCopy;
					}
					queue.add(knCopy);
				}
				//Dont use item[kn.level]
				kn.bound=calcBound(nrItems, itemOrder, itemValues, itemWeights, kn.level+1, maxKnapsackWeight-kn.weight, kn.value);
				queue.add(kn);
			}
		}
		
		if(bestNode==null) //Greedy solution was the best
			return greedySolution;
		else{
			return new KnapsackResult(bestNode.selectedItems, bestNode.weight, bestNode.value);
		}
	}
	
	/**
	 * Calculate a bound on the best solution attainable for a given partial solution. 
	 * @return
	 */
	private static double calcBound(int nrItems, Integer[] itemOrder, double[] itemValues, int[] itemWeights, int level, int remainingSize, double value){
		double bound=value;
		while(level<nrItems && remainingSize-itemWeights[itemOrder[level]]>=0){
			remainingSize-=itemWeights[itemOrder[level]];
			bound+=itemValues[itemOrder[level]];
			level++;
		}
		if(level<nrItems){
			bound+=itemValues[itemOrder[level]]*(remainingSize/(double)itemWeights[itemOrder[level]]);
		}
		return bound;
	}
	
	/**
	 * Simple class which reflects the solution of a knapsack problem
	 *
	 */
	public static final class KnapsackResult{
		public final boolean[] selectedItems; //Selected items
		public final int weight;
		public final double value;
	
		public KnapsackResult(boolean[] selectedItems, int weight, double value){
			this.selectedItems=selectedItems;
			this.weight=weight;
			this.value=value;
		}
		public String toString(){
			String s="value: "+value+" weight: "+weight+"\nitems: "+Arrays.toString(selectedItems);
			return s;
		}
	}
	
	/**
	 * Knapsack nodes represent partial solutions for the knapsack problem. A subset of the variables, starting from the root node of the tree up to <level> level,
	 * have been fixed. 
	 * @author jkinable
	 *
	 */
	private static final class KnapsackNode implements Comparable<KnapsackNode>{
		public final int nrItems; //Max number of items in the problem
		public final boolean[] selectedItems; //Selected items
		public int level; //Depth of the knapsack node in the search tree; Each level of the search tree corresponds with a single item.
		public double bound; //Bound on the optimum value atainable by this node
		public double value; //Total value of the items in this knapsack 
		public int weight; //Total weight of the items in this knapsack
		
		public KnapsackNode(int nrItems){
			this.nrItems=nrItems;
			selectedItems=new boolean[nrItems];
			this.bound=0;
			this.value=0;
			this.weight=0;
			this.level=-1;
		}
		
		public KnapsackNode(int nrItems, int level, double bound, double value, int weight, boolean[] selectedItems) {
			this.nrItems=nrItems;
			this.level=level;
			this.bound=bound;
			this.value=value;
			this.weight=weight;
			this.selectedItems=selectedItems;
		}
	
		public KnapsackNode copy(){
			boolean[] selectedItemsCopy=new boolean[nrItems];
			System.arraycopy(selectedItems, 0, selectedItemsCopy, 0, nrItems);
			return new KnapsackNode(nrItems, this.level, this.bound, this.value, this.weight, selectedItemsCopy);
		}
		
		public void addItem(int itemID, int itemWeight, double itemValue){
			selectedItems[itemID]=true;
			weight+=itemWeight;
			value+=itemValue;
		}
		
		@Override
		public int compareTo(KnapsackNode otherNode) {
			if(this.bound==otherNode.bound)
				return 0;
			else if(this.bound>otherNode.bound)
				return -1;
			else
				return 1;
		}
		
		public String toString(){
			return "Level: "+level+" bound: "+bound+" value: "+value+" weight: "+weight+" items: "+Arrays.toString(selectedItems)+" \n";
		}
	
	}
	
	public static void main(String[] args){
		/**
		 * Knapsacksize: 2000
		 *   item    profit  weight  take
		 * 1       874     580     true
		 *  2       620     1616    false
		 *  3       345     1906    false
		 *  4       369     1942    false
		 *  5       360     50      true
		 *  6       470     294     true
		 */
		
//		double[] itemValues={300, 60, 90, 100, 240};
//		int[] itemWeights={50, 10, 20, 40, 30};
//		int maxKnapsackWeight=60;
		//Correct solution: value 390, items: [false, true, true, false, true]
		
		double[] itemValues={15,10,9,5};
		int[] itemWeights={1,5,3,4};
		int maxKnapsackWeight=8;
		//Correct solution: value 29, items: [true, false, true, true]
		
		KnapsackResult solution=runKnapsack(itemValues.length, maxKnapsackWeight, itemValues, itemWeights);
		System.out.println("Knapsack solution: "+solution);
	}	
}

