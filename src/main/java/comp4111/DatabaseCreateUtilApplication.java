package comp4111;

import comp4111.dal.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseCreateUtilApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCreateUtilApplication.class);

    public static void main(String[] args) {
        try {
            DatabaseConnection.setConfig();
        } finally {
            DatabaseConnection.cleanUp();
            LOGGER.info("The database connection is closed");
        }
    }
}
