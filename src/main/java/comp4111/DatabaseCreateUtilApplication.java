package comp4111;

import comp4111.dal.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseCreateUtilApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCreateUtilApplication.class);

    public static void main(String[] args) {
        try {
            DatabaseUtils.setupSchemas(true);
        } finally {
            LOGGER.info("The database connection is closed");
        }
    }
}
