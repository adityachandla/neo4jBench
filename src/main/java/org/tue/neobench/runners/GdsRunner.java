package org.tue.neobench.runners;

import org.apache.commons.cli.CommandLine;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.tue.neobench.Constants;
import org.tue.neobench.query.IntervalMap;
import org.tue.neobench.query.QueryDTO;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GdsRunner implements Runner {
    private static final String CREATE_GRAPH_QUERY = """
            call gds.graph.project(
            'personKnowsGraph',
            'NODE',
            'personKnows'
            )
            """;
    private static final String DROP_GRAPH_QUERY = """
            call gds.graph.drop('personKnowsGraph', false)
            """;
    private static final String BFS_QUERY = """
            match (s:NODE {uid: $id})
            call gds.bfs.stream('personKnowsGraph', {sourceNode: s, maxDepth: $depth}) yield nodeIds
            return size(nodeIds);
            """;
    private static final String DFS_QUERY = """
            match (s:NODE {uid: $id})
            call gds.dfs.stream('personKnowsGraph', {sourceNode: s, maxDepth: $depth}) yield nodeIds
            return size(nodeIds);
            """;
    private final int DEPTH_MIN = 1;
    private final int DEPTH_MAX = 4;
    private final CommandLine cli;
    private final Driver driver;
    private final QueryConfig config;
    private final Random r;

    public GdsRunner(CommandLine cli, Random r) {
        this.cli = cli;
        var authToken = AuthTokens.basic(Constants.NEO4J_USERNAME, Constants.NEO4J_PASSWORD);
        this.driver = GraphDatabase.driver(Constants.NEO4J_URI, authToken);
        this.r = r;
        this.driver.verifyConnectivity();
        this.config = QueryConfig.builder().withDatabase(Constants.DATABASE_NAME).build();
        this.driver.executableQuery(DROP_GRAPH_QUERY).withConfig(this.config).execute();
        this.driver.executableQuery(CREATE_GRAPH_QUERY).withConfig(this.config).execute();
    }

    @Override
    public void runQueries(List<QueryDTO> queries) {
        //parameter queries is ignored.
        var intervalMap = IntervalMap.fromFile(Util.getResourceInputStream("nodeMap1.csv"));
        var personInterval = intervalMap.valueOf("person");
        String queryStr = "BFS".equals(cli.getOptionValue("algorithm")) ? BFS_QUERY : DFS_QUERY;
        for (int depth = DEPTH_MIN; depth <= DEPTH_MAX; depth++) {
            for (int j = 0; j < Constants.REPETITION; j++) {
                int nodeId = r.nextInt(personInterval.getStart(), personInterval.getEnd());
                var queryParams = Map.<String, Object>of("id", nodeId, "depth", depth);
                long start = System.nanoTime();
                var result = driver.executableQuery(queryStr)
                        .withConfig(this.config)
                        .withParameters(queryParams)
                        .execute();

                long time = (System.nanoTime()-start)/1000;
                int count = result.records().getFirst().get(0).asInt();
                System.out.printf("QueryDepth=%d,RunningTime=%d,NumRes=%d\n", depth, time,
                        count);
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.driver.close();
    }
}
