package org.tue.neobench.query;

import org.neo4j.graphdb.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern sourcePattern = Pattern.compile("^(\\w+)");
    private static final Pattern edgePattern = Pattern.compile("\\((.+?),\\s?(\\w+)\\)");

    public static QueryDTO parseQuery(String line) {
        var srcMatcher = sourcePattern.matcher(line);
        if (!srcMatcher.find()) {
            throw new IllegalArgumentException("Source not found");
        }
        String name = srcMatcher.group(1);

        var edgeMatcher = edgePattern.matcher(line);
        List<EdgePath> labels = new ArrayList<>();
        while(edgeMatcher.find()) {
            var directionStr = edgeMatcher.group(1);
            Direction dir = switch (directionStr) {
                case "<>" -> Direction.BOTH;
                case ">" -> Direction.OUTGOING;
                case "<" -> Direction.INCOMING;
                case null, default -> throw new IllegalArgumentException("Invalid direction string: " + directionStr);
            };
            var label = edgeMatcher.group(2);
            labels.add(new EdgePath(label, dir));
        }
        return new QueryDTO(name, labels);
    }
}

