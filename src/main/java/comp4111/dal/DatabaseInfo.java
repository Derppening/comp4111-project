package comp4111.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class DatabaseInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInfo.class);

    /**
     * The URL to the MySQL database.
     */
    public static final String MYSQL_URL;
    /**
     * The username used to login.
     */
    public static final String MYSQL_LOGIN;
    /**
     * The password of the user.
     */
    public static final String MYSQL_PASSWORD;
    /**
     * The name of the database.
     */
    public static final String DB_NAME;

    static {
        final var classLoader = Thread.currentThread().getContextClassLoader();
        final var mysqlProperties = new Properties();
        try {
            mysqlProperties.load(classLoader.getResourceAsStream("mysql.properties"));
        } catch (IOException e) {
            LOGGER.error("Cannot read from database properties", e);
            System.exit(1);
        }

        MYSQL_URL = mysqlProperties.get("mysql.url").toString();
        MYSQL_LOGIN = mysqlProperties.get("mysql.username").toString();
        MYSQL_PASSWORD = mysqlProperties.get("mysql.password").toString();
        DB_NAME = mysqlProperties.get("mysql.database").toString();
    }

    private DatabaseInfo() {
    }
}
