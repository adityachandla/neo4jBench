package org.tue.neobench;

import java.nio.file.Path;

public class Constants {
    public static final Path DATABASE_DIRECTORY = Path.of("/var/lib/neo4j/");
    public static final Path CONFIG_DIRECTORY = Path.of("/etc/neo4j/neo4j.conf");
    public static final String DATABASE_NAME = "neo4j1";
    public static final Integer REPETITION = 20;
}
