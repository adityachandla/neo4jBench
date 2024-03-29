package org.tue.neobench;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
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
        Options opts = getCliOptions();
        var cli = new DefaultParser().parse(opts, args);

        var queries = readQueries();

        Random rand = new Random(17041998);
        try (var runner = getRunner(cli, rand)) {
            runner.runQueries(queries);
        }
    }

    private static Runner getRunner(CommandLine cli, Random rand) {
        return switch (cli.getOptionValue("method")) {
            case "lowLevel" -> new LowLevelRunner(cli, rand);
            case "traversal" -> new TraversalRunner(cli, rand);
            case "cypher" -> new CypherRunner(rand);
            case "gds" -> new GdsRunner(cli, rand);
            case "pregel" -> new PregelRunner(rand);
            default -> throw new IllegalArgumentException();
        };
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
        opts.addOption("m", "method", true, "lowLevel/traversal/cypher/gds/pregel");
        return opts;
    }

}