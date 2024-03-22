package org.tue.neobench.algos;

import org.neo4j.graphdb.Transaction;

public interface Traversal {
    //Return running time in microseconds.
    long getRunningTime(long startNode, Transaction tx);
}
