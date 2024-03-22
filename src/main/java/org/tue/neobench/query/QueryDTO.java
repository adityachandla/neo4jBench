package org.tue.neobench.query;

import java.util.List;

public record QueryDTO(String startNodeName, List<EdgePath> paths) {
}
