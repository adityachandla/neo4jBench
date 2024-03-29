package org.tue.neobench.runners;

import org.tue.neobench.query.QueryDTO;

import java.util.List;

public interface Runner extends AutoCloseable {
    void runQueries(List<QueryDTO> queries);
}
