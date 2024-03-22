package org.tue.neobench;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.glassfish.jersey.internal.inject.Custom;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.tue.neobench.algos.BFS;
import org.tue.neobench.algos.CustomEvaluator;
import org.tue.neobench.algos.DFS;
import org.tue.neobench.algos.Traversal;
import org.tue.neobench.query.EdgePath;
import org.tue.neobench.query.IntervalMap;
import org.tue.neobench.query.QueryDTO;
import org.tue.neobench.query.QueryParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private static final Random r = new Random(17041998);
    private static GraphDatabaseService db;

    public static void main(String[] args) throws Exception {
        var managementService = new DatabaseManagementServiceBuilder(Constants.DATABASE_DIRECTORY)
                .loadPropertiesFromFile(Constants.CONFIG_DIRECTORY)
                .setConfig(GraphDatabaseSettings.read_only_database_default, true)
                .build();

        Options opts = getCliOptions();
        var cli = new DefaultParser().parse(opts, args);
        db = managementService.database(Constants.DATABASE_NAME);
        var queries = readQueries();
        if ("lowLevel".equals(cli.getOptionValue("method"))) {
            runUsingLowLevelAPI(cli, queries);
        } else if ("traversal".equals(cli.getOptionValue("method"))) {
            runUsingTraversalAPI(cli, queries);
        }
        managementService.shutdown();
    }

    private static void runUsingTraversalAPI(CommandLine cli, List<QueryDTO> queries) {
        var intervalMap = IntervalMap.fromFile(getResourceInputStream("nodeMap1.csv"));
        int queryIdx = 1;
        for (var q : queries) {
            var interval = intervalMap.valueOf(q.startNodeName());
            for (int i = 0; i < Constants.REPETITION; i++) {
                var nodeUid = interval.getStart() + r.nextInt(interval.getEnd() - interval.getStart());
                Node startNode;
                try(var tx = db.beginTx()) {
                    startNode = tx.findNode(Label.label("NODE"), "uid", nodeUid);
                    TraversalDescription desc = tx.traversalDescription()
                            .breadthFirst();
                    for (var l : q.paths()) {
                        desc.relationships(RelationshipType.withName(l.label()), l.dir());
                    }
                    long start = System.nanoTime();
                    var traverser = desc.evaluator(new CustomEvaluator(q.paths())).traverse(startNode);
                    Set<Long> finalNodes = new HashSet<>();
                    for (var path: traverser) {
                        finalNodes.add((Long)path.endNode().getProperty("uid"));
                    }
                    System.out.println(finalNodes.size());
                    long time = (System.nanoTime() - start) / 1000;
                    System.out.printf("QueryId=%d,RunningTime=%d\n", queryIdx, time);
                }
            }
            queryIdx++;
        }
    }

    private static void runUsingLowLevelAPI(CommandLine cli, List<QueryDTO> queries) {
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

    private static void runQueries(List<QueryDTO> queries, TraversalGetter traversal) {
        var intervalMap = IntervalMap.fromFile(getResourceInputStream("nodeMap1.csv"));
        int queryIdx = 1;
        for (var q : queries) {
            var interval = intervalMap.valueOf(q.startNodeName());
            var traversalAlgo = traversal.get(q.paths());
            for (int i = 0; i < Constants.REPETITION; i++) {
                int startNode = interval.getStart() + r.nextInt(interval.getEnd() - interval.getStart());
                try (var tx = db.beginTx()) {
                    long time = traversalAlgo.getRunningTime(startNode, tx);
                    System.out.printf("QueryId=%d,RunningTime=%d\n", queryIdx, time);
                }
            }
            queryIdx++;
        }
    }

    private static List<QueryDTO> readQueries() throws Exception {
        var reader = new BufferedReader(new InputStreamReader(getResourceInputStream("queries.txt")));
        List<QueryDTO> queries = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            if (line.isBlank()) {
                line = reader.readLine();
                continue;
            }
            var q = QueryParser.parseQuery(line);
            queries.add(q);
            line = reader.readLine();
        }
        return queries;

    }

    private static InputStream getResourceInputStream(String fileName) {
        var inputStream = Main.class.getClassLoader()
                .getResourceAsStream(fileName);
        Objects.requireNonNull(inputStream);
        return inputStream;
    }

    private static Options getCliOptions() {
        var opts = new Options();
        opts.addOption("a", "algorithm", true, "DFS or BFS");
        opts.addOption("m", "method", true, "lowLevel/traversal");
        return opts;
    }


    @FunctionalInterface
    interface TraversalGetter {
        Traversal get(List<EdgePath> paths);
    }

}