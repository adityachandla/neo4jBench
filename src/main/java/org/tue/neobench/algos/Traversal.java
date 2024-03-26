package org.tue.neobench.algos;

import org.neo4j.graphdb.Transaction;
import org.tue.neobench.query.QueryRes;

public interface Traversal {
    //Return running time in microseconds.
    QueryRes getQueryRes(long startNode, Transaction tx);
}
