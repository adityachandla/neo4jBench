package org.tue.neobench.runners;

import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.tue.neobench.Constants;
import org.tue.neobench.query.IntervalMap;
import org.tue.neobench.query.QueryDTO;
import org.tue.neobench.query.QueryRes;

import java.util.List;
import java.util.Map;
import java.util.Random;


@AllArgsConstructor
public class CypherRunner implements Runner {
    private GraphDatabaseService db;
    private CommandLine cli;
    private Random r;

    private static final String queryStart = "Match (n:NODE {uid: $id})";
    private static final String queryEnd = " return count (distinct d.uid) as cnt";

    @Override
    public void runQueries(List<QueryDTO> queries) {
        var intervalMap = IntervalMap.fromFile(Util.getResourceInputStream("nodeMap1.csv"));
        int queryIdx = 1;
        for (var query: queries) {
            var interval = intervalMap.valueOf(query.startNodeName());
            var queryStr = generateCypher(query);
            for (int i = 0; i < Constants.REPETITION; i++) {
                var nodeUid = interval.getStart() + r.nextInt(interval.getEnd() - interval.getStart());
                var queryRes = runQueryForNode(queryStr, nodeUid);
                System.out.printf("QueryId=%d,RunningTime=%d,NumRes=%d\n", queryIdx, queryRes.time(),
                        queryRes.numNodes());
            }
            queryIdx++;
        }
    }

    private QueryRes runQueryForNode(String query, int startNodeId) {
        try (var tx = db.beginTx()) {
            var start = System.nanoTime();
            var res = tx.execute(query, Map.of("id", startNodeId));
            var duration = (System.nanoTime()-start)/1000;
            Map<String, Object> row = res.next();
            long countString = (Long)row.get("cnt");
            return new QueryRes(duration, (int)countString);
        }
    }

    private String generateCypher(QueryDTO query) {
        StringBuilder q = new StringBuilder(queryStart);
        for (int i = 0; i < query.paths().size(); i++) {
            if (query.paths().get(i).dir() == Direction.INCOMING) {
                q.append("<-");
            } else {
                q.append('-');
            }
            q.append(String.format("[:%s]", query.paths().get(i).label()));
            if (query.paths().get(i).dir() == Direction.OUTGOING) {
                q.append("->");
            } else {
                q.append('-');
            }
            if (i == query.paths().size()-1) {
                q.append("(d:NODE)");
            } else {
                q.append("()");
            }
        }
        q.append(queryEnd);
        return q.toString();
    }
}
