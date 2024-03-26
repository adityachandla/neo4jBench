package org.tue.neobench.algos;

import lombok.AllArgsConstructor;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.tue.neobench.query.EdgePath;

import java.util.List;

@AllArgsConstructor
@Deprecated
public class CustomEvaluator implements Evaluator {

    private final List<EdgePath> pathList;

    @Override
    public Evaluation evaluate(Path path) {
        int pathLength = path.length();
        if (path.length() == 0) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        if (pathLength == pathList.size() && isSame(path.lastRelationship(), pathList.getLast())) {
            return Evaluation.INCLUDE_AND_PRUNE;
        } else if (pathLength == pathList.size()) {
            return Evaluation.EXCLUDE_AND_PRUNE;
        }
        var edgeToMatch = pathList.get(pathLength - 1);
        if (isSame(path.lastRelationship(), edgeToMatch)) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        return Evaluation.EXCLUDE_AND_PRUNE;
    }

    private boolean isSame(Relationship rel, EdgePath path) {
        return rel.isType(RelationshipType.withName(path.label()));
    }
}
