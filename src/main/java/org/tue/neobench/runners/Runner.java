package org.tue.neobench.runners;

import org.tue.neobench.query.QueryDTO;

import java.util.List;

public interface Runner {
    void runQueries(List<QueryDTO> queries);
}
