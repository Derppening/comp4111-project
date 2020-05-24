package comp4111;

import comp4111.dal.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DatabaseDropUtilApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDropUtilApplication.class);

    public static void main(String[] args) {
        try {
            DatabaseUtils.dropDatabase();
        } catch (SQLException e) {
            LOGGER.error("Unable to drop database", e);
        }
    }
}
