package org.tue.neobench.algos;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.tue.neobench.query.EdgePath;

import java.util.*;

public class BFS implements Traversal {
    private final List<EdgePath> paths;
    public BFS(List<EdgePath> paths) {
        this.paths = paths;
    }

    public long getRunningTime(long startNode, Transaction tx) {
        Queue<Long> bfsQueue = new LinkedList<>();
        bfsQueue.add(startNode);
        long start = System.nanoTime();
        for (var path: paths) {
            Queue<Long> nextQueue = new LinkedList<>();
            Set<Long> seen = new HashSet<>();
            while (!bfsQueue.isEmpty()) {
                Long nodeId = bfsQueue.poll();
                var node = tx.findNode(Label.label("NODE"), "uid", nodeId);
                var edges = node.getRelationships(path.dir(), RelationshipType.withName(path.label()));
                for (var edge : edges) {
                    var uid = (Long) edge.getOtherNode(node).getProperty("uid");
                    if (!seen.contains(uid)) {
                        seen.add(uid);
                        nextQueue.add(uid);
                    }
                }
            }
            bfsQueue = nextQueue;
        }
        return (System.nanoTime()-start)/1000;
    }
}
