/* ==========================================
 * jORLib : Java Operations Research Library
 * ==========================================
 *
 * Project Info:  http://www.coin-or.org/projects/jORLib.xml
 * Project Creator:  Joris Kinable (https://github.com/jkinable)
 *
 * (C) Copyright 2016-2016, by Joris Kinable and Contributors.
 *
 * This program and the accompanying materials are licensed under LGPLv2.1
 * as published by the Free Software Foundation.
 */
package org.jorlib.demo.frameworks.columngeneration.graphcoloringbap.bap.branching.branchingDecisions;

import org.jgrapht.util.VertexPair;
import org.jorlib.demo.frameworks.columngeneration.graphcoloringbap.cg.IndependentSet;
import org.jorlib.demo.frameworks.columngeneration.graphcoloringbap.model.ColoringGraph;
import org.jorlib.frameworks.columngeneration.branchandprice.branchingdecisions.BranchingDecision;
import org.jorlib.frameworks.columngeneration.master.cutGeneration.AbstractInequality;

/**
 * Ensure that two vertices are assigned the same color
 * 
 * @author Joris Kinable
 * @version 29-6-2016
 */
public final class SameColor
    implements BranchingDecision<ColoringGraph, IndependentSet>
{

    /** Vertices to be colored the same **/
    public final VertexPair<Integer> vertexPair;

    public SameColor(VertexPair<Integer> vertexPair)
    {
        this.vertexPair = vertexPair;
    }

    /**
     * Determine whether the given column remains feasible for the child node
     * 
     * @param column column
     * @return true if the column is compliant with the branching decision
     */
    @Override
    public boolean columnIsCompatibleWithBranchingDecision(IndependentSet column)
    {
        return !(column.vertices.contains(vertexPair.getFirst())
            ^ column.vertices.contains(vertexPair.getSecond()));
    }

    /**
     * Determine whether the given inequality remains feasible for the child node
     * 
     * @param inequality inequality
     * @return true
     */
    @Override
    public boolean inEqualityIsCompatibleWithBranchingDecision(AbstractInequality inequality)
    {
        return true; // Cuts are not added in this example
    }

    @Override
    public String toString()
    {
        return "Samecolor " + vertexPair;
    }
}