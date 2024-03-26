package org.tue.neobench.runners;

import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.tue.neobench.Constants;
import org.tue.neobench.algos.BFS;
import org.tue.neobench.algos.DFS;
import org.tue.neobench.algos.Traversal;
import org.tue.neobench.query.EdgePath;
import org.tue.neobench.query.IntervalMap;
import org.tue.neobench.query.QueryDTO;

import java.util.List;
import java.util.Random;

@AllArgsConstructor
public class LowLevelRunner implements Runner {
    private GraphDatabaseService db;
    private CommandLine cli;
    private Random r;

    @Override
    public void runQueries(List<QueryDTO> queries) {
        TraversalGetter traversalAlgo;
        if ("BFS".equals(cli.getOptionValue("algorithm"))) {
            traversalAlgo = BFS::new;
        } else if ("DFS".equals(cli.getOptionValue("algorithm"))) {
            traversalAlgo = DFS::new;
        } else {
            throw new IllegalArgumentException("Invalid algorithm");
        }
        runQueries(queries, traversalAlgo);
    }

    private void runQueries(List<QueryDTO> queries, TraversalGetter traversal) {
        var intervalMap = IntervalMap.fromFile(Util.getResourceInputStream("nodeMap1.csv"));
        int queryIdx = 1;
        for (var q : queries) {
            var interval = intervalMap.valueOf(q.startNodeName());
            var traversalAlgo = traversal.get(q.paths());
            for (int i = 0; i < Constants.REPETITION; i++) {
                int startNode = interval.getStart() + r.nextInt(interval.getEnd() - interval.getStart());
                try (var tx = db.beginTx()) {
                    var queryRes = traversalAlgo.getQueryRes(startNode, tx);
                    System.out.printf("QueryId=%d,RunningTime=%d,NumRes=%d\n", queryIdx, queryRes.time(),
                            queryRes.numNodes());
                }
            }
            queryIdx++;
        }
    }

    @FunctionalInterface
    interface TraversalGetter {
        Traversal get(List<EdgePath> paths);
    }
}
