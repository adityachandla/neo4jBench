package org.tue.neobench.runners;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.tue.neobench.Constants;

import java.io.InputStream;
import java.util.Objects;

public class Util {

    public static DatabaseManagementService getManagementService() {
        return new DatabaseManagementServiceBuilder(Constants.DATABASE_DIRECTORY)
                .loadPropertiesFromFile(Constants.CONFIG_DIRECTORY)
                .setConfig(GraphDatabaseSettings.read_only_database_default, true)
                .build();

    }

    public static GraphDatabaseService getGraphDb(DatabaseManagementService managementService) {
        return managementService.database(Constants.DATABASE_NAME);
    }

    public static InputStream getResourceInputStream(String fileName) {
        var inputStream = Util.class.getClassLoader()
                .getResourceAsStream(fileName);
        Objects.requireNonNull(inputStream);
        return inputStream;
    }
}
