package org.tue.neobench.query;

import lombok.SneakyThrows;
import org.apache.commons.cli.DefaultParser;
import org.junit.jupiter.api.*;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.tue.neobench.Main;
import org.tue.neobench.runners.TraversalRunner;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TraversalRunnerTest {

    private static final String DEFAULT_DATABASE_NAME = "neo4j";
    private static final Path DIRECTORY = Path.of("trialGraph");
    private static GraphDatabaseService db;
    private static DatabaseManagementService managementService;

    public static void init() {
        managementService = new DatabaseManagementServiceBuilder(DIRECTORY).build();
        db = managementService.database(DEFAULT_DATABASE_NAME);
        Assertions.assertTrue(db.isAvailable());
    }

    public static void teardown() {
        try (var tx = db.beginTx()) {
            tx.getAllNodes().forEach(n -> {
                n.getRelationships().forEach(Relationship::delete);
                n.delete();
            });
            tx.commit();
        }
        managementService.shutdown();
    }

    private void addData() {
        try (var tx = db.beginTx()) {
            var one = tx.createNode(Label.label("NODE"));
            one.setProperty("uid", 1L);
            var two = tx.createNode(Label.label("NODE"));
            two.setProperty("uid", 2L);
            var three = tx.createNode(Label.label("NODE"));
            three.setProperty("uid", 3L);
            var four = tx.createNode(Label.label("NODE"));
            four.setProperty("uid", 4L);

            one.createRelationshipTo(two, RelationshipType.withName("personKnows"));
            one.createRelationshipTo(three, RelationshipType.withName("personKnows"));

//            three.createRelationshipTo(one, RelationshipType.withName("personKnows"));
//            four.createRelationshipTo(two, RelationshipType.withName("personKnows"));
            tx.commit();
        }
    }

    @Test
    public void testTraversal() {
        try {
            init();
            addData();
            String[] args = {"-a=BFS", "-m=traversal"};
            var cli = new DefaultParser().parse(Main.getCliOptions(), args);
            var t = new TraversalRunner(db, cli, new Random());
            var ep = new EdgePath("personKnows", Direction.BOTH);
            var desc = t.getTraversalDescription(new QueryDTO("1",
                    List.of(ep, ep)));
            var res = t.runQueryForNode(desc, 1);
            System.out.println(res.numNodes());
            System.out.println(res.time());
        }catch (Exception e){
            e.printStackTrace();
            Assertions.fail();
        } finally {
            teardown();
        }
    }
}
