package org.tue.neobench.runners;

import org.apache.commons.cli.CommandLine;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.tue.neobench.Constants;
import org.tue.neobench.algos.CustomPathExpander;
import org.tue.neobench.query.IntervalMap;
import org.tue.neobench.query.QueryDTO;
import org.tue.neobench.query.QueryRes;

import java.util.List;
import java.util.Random;

public class TraversalRunner implements Runner {
    private final GraphDatabaseService db;
    private final DatabaseManagementService managementService;
    private final CommandLine cli;
    private final Random r;

    public TraversalRunner(CommandLine cli, Random r) {
        this.managementService = Util.getManagementService();
        this.db = Util.getGraphDb(this.managementService);
        this.cli = cli;
        this.r = r;
    }

    private static int countNodes(Traverser t) {
        int i = 0;
        for (var v : t.nodes()) {
            i++;
        }
        return i;
    }

    @Override
    public void runQueries(List<QueryDTO> queries) {
        var intervalMap = IntervalMap.fromFile(Util.getResourceInputStream("nodeMap1.csv"));
        int queryIdx = 1;
        for (var q : queries) {
            var interval = intervalMap.valueOf(q.startNodeName());
            var traversalDesc = getTraversalDescription(q);
            for (int i = 0; i < Constants.REPETITION; i++) {
                var nodeUid = interval.getStart() + r.nextInt(interval.getEnd() - interval.getStart());
                var queryRes = runQueryForNode(traversalDesc, nodeUid);
                System.out.printf("QueryId=%d,RunningTime=%d,NumRes=%d\n", queryIdx, queryRes.time(),
                        queryRes.numNodes());
            }
            queryIdx++;
        }
    }

    public QueryRes runQueryForNode(TraversalDescription desc, int nodeId) {
        try (var tx = db.beginTx()) {
            var startNode = tx.findNode(Label.label("NODE"), "uid", nodeId);
            long start = System.nanoTime();
            var traverser = desc.traverse(startNode);
            int numNodes = countNodes(traverser);
            long duration = (System.nanoTime() - start) / 1000;
            return new QueryRes(duration, numNodes);
        }
    }

    public TraversalDescription getTraversalDescription(QueryDTO query) {
        try (var tx = db.beginTx()) {
            TraversalDescription desc = switch (cli.getOptionValue("algorithm")) {
                case "BFS" -> tx.traversalDescription().breadthFirst();
                case "DFS" -> tx.traversalDescription().depthFirst();
                default -> throw new IllegalArgumentException();
            };
            return desc
                    .uniqueness(Uniqueness.NODE_LEVEL)
                    .evaluator(Evaluators.atDepth(query.paths().size()))
                    .expand(new CustomPathExpander(query.paths()));
        }
    }

    @Override
    public void close() throws Exception {
        managementService.shutdown();
    }
}
