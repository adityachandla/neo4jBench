package org.tue.neobench.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Direction;

public class QueryParserTest {

    @Test
    public void testQueryParsing() {
        String queryLine = "Post (>, postContainerForum)(<>, forumTag)(<, tagClass)";
        var queryDTO = QueryParser.parseQuery(queryLine);
        Assertions.assertEquals(queryDTO.paths().size(), 3);

        Assertions.assertEquals(queryDTO.paths().getFirst().dir(), Direction.OUTGOING);
        Assertions.assertEquals(queryDTO.paths().get(1).dir(), Direction.BOTH);
        Assertions.assertEquals(queryDTO.paths().getLast().dir(), Direction.INCOMING);
    }
}
