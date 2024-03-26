package org.tue.neobench.algos;

import lombok.AllArgsConstructor;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterables;
import org.tue.neobench.query.EdgePath;

import java.util.List;

@AllArgsConstructor
public class CustomPathExpander implements PathExpander<Void> {

    private List<EdgePath> paths;

    @Override
    public ResourceIterable<Relationship> expand(Path path, BranchState<Void> state) {
        if (path.length() == paths.size()) {
            return Iterables.emptyResourceIterable();
        }
        var pathToConsider = paths.get(path.length());
        var nextNodes = path.endNode()
                .getRelationships(pathToConsider.dir(), RelationshipType.withName(pathToConsider.label()));
        return Iterables.asResourceIterable(nextNodes);
    }

    @Override
    public PathExpander<Void> reverse() {
        throw new RuntimeException("Only MonoDirectional traversal implemented");
    }
}
