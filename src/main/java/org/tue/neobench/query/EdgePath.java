package org.tue.neobench.query;

import org.neo4j.graphdb.Direction;

public record EdgePath(String label, Direction dir) {
}
