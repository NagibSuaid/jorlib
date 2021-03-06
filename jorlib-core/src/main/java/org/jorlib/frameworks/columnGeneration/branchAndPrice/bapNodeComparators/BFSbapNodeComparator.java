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
 * BFSbapNodeComparator.java
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
package org.jorlib.frameworks.columnGeneration.branchAndPrice.bapNodeComparators;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.BAPNode;

import java.util.Comparator;

/**
 * Simple comparator which processes the BAP tree in a BFS manner. The nodes are sorted based on their nodeID.
 *
 * @author Joris Kinable
 * @version 5-5-2015
 */
public class BFSbapNodeComparator implements Comparator<BAPNode>{
    @Override
    public int compare(BAPNode o1, BAPNode o2) {
        return Integer.compare(o1.nodeID, o2.nodeID);
    }
}
