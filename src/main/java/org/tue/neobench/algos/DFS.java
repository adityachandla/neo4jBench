package org.tue.neobench.algos;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.tue.neobench.query.EdgePath;

import java.util.*;

public class DFS implements Traversal {
    private final List<EdgePath> paths;

    public DFS(List<EdgePath> paths) {
        this.paths = paths;
    }

    public long getRunningTime(long startNode, Transaction tx) {
        List<Long> result = new ArrayList<>();
        Deque<NodeLevel> dfsStack = new LinkedList<>();
        Set<NodeLevel> seen = new HashSet<>();
        dfsStack.push(new NodeLevel(startNode, 0));
        long start = System.nanoTime();
        while (!dfsStack.isEmpty()) {
            var toProcess = dfsStack.pop();
            var node = tx.findNode(Label.label("NODE"), "uid", toProcess.node);
            var pathInfo = paths.get(toProcess.level());
            var rels = node.getRelationships(pathInfo.dir(), RelationshipType.withName(pathInfo.label()));
            for (var rel : rels) {
                var nodeId = (Long) rel.getOtherNode(node).getProperty("uid");
                var nodeLevel = new NodeLevel(nodeId, toProcess.level()+1);
                if (nodeLevel.level() == paths.size()) {
                    result.add(nodeId);
                } else if (!seen.contains(nodeLevel)) {
                    seen.add(nodeLevel);
                    dfsStack.push(nodeLevel);
                }
            }
        }
        return (System.nanoTime() - start)/1000;
    }

    private record NodeLevel(long node, int level) {
    }
}
