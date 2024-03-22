package org.tue.neobench;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

public class ConnectionTest {
    public static void main(String[] args) {
        var token = AuthTokens.basic("neo4j", "hello123");
        var uri = "neo4j://localhost";
        try (var driver = GraphDatabase.driver(uri, token)) {
            driver.verifyConnectivity();
            System.out.println("Verified");
        }
    }
}
