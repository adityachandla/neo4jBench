package org.tue.neobench.runners;

import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.tue.neobench.query.QueryDTO;

import java.util.List;
import java.util.Random;


@AllArgsConstructor
public class CypherRunner implements Runner {
    private GraphDatabaseService db;
    private CommandLine cli;
    private Random r;

    @Override
    public void runQueries(List<QueryDTO> queries) {

    }
}
