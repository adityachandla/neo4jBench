package org.tue.neobench;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.tue.neobench.query.QueryDTO;
import org.tue.neobench.query.QueryParser;
import org.tue.neobench.runners.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws Exception {
        var managementService = new DatabaseManagementServiceBuilder(Constants.DATABASE_DIRECTORY)
                .loadPropertiesFromFile(Constants.CONFIG_DIRECTORY)
                .setConfig(GraphDatabaseSettings.read_only_database_default, true)
                .build();
        GraphDatabaseService db = managementService.database(Constants.DATABASE_NAME);

        Options opts = getCliOptions();
        var cli = new DefaultParser().parse(opts, args);

        var queries = readQueries();

        Random rand = new Random(17041998);
        Runner runner = switch (cli.getOptionValue("method")) {
            case "lowLevel" -> new LowLevelRunner(db, cli, rand);
            case "traversal" -> new TraversalRunner(db, cli, rand);
            case "cypher" -> new CypherRunner(db, cli, rand);
            default -> throw new IllegalArgumentException();
        };

        runner.runQueries(queries);

        managementService.shutdown();
    }

    private static List<QueryDTO> readQueries() throws Exception {
        var reader = new BufferedReader(new InputStreamReader(Util.getResourceInputStream("queries.txt")));
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

    public static Options getCliOptions() {
        var opts = new Options();
        opts.addOption("a", "algorithm", true, "DFS or BFS");
        opts.addOption("m", "method", true, "lowLevel/traversal/cypher");
        return opts;
    }

}