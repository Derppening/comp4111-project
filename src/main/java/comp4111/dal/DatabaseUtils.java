package comp4111.dal;

import comp4111.util.SecurityUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import static comp4111.dal.DatabaseInfo.*;

/**
 * Utilities for database-related operations.
 */
public class DatabaseUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);

    /**
     * Name for the User Credentials table.
     */
    private static final String NAME_USER_CREDENTIALS = "User_Credentials";
    @Language(value = "SQL", prefix = "CREATE TABLE ")
    private static final String SCHEMA_USER_CREDENTIALS = NAME_USER_CREDENTIALS + "(" +
            "username VARCHAR(40) PRIMARY KEY," +
            "hashed_password VARCHAR(64)," +
            "salt VARCHAR(32)" +
            ");";

    /**
     * Name for the Books table.
     */
    private static final String NAME_BOOK = "Book";
    @Language(value = "SQL", prefix = "CREATE TABLE ")
    private static final String SCHEMA_BOOK = NAME_BOOK + "(" +
            "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "title VARCHAR(80) UNIQUE," +
            "author VARCHAR(80)," +
            "publisher VARCHAR(80)," +
            "year INT," +
            "available TINYINT" +
            ");";

    private DatabaseUtils() {
    }

    /**
     * @return A database connection to the SQL server.
     */
    @NotNull
    private static Connection getServerConnection() {
        try {
            return DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
        } catch (SQLException e) {
            LOGGER.error("Cannot get connection to SQL server", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and setups schemas for the database.
     *
     * @param overwriteAll If {@code true}, overwrites all existing schemas and data.
     * @return {@code true} if no error was encountered.
     */
    public static boolean setupSchemas(boolean overwriteAll) {
        final var db = createDatabaseSchema(overwriteAll);
        final var tables = createTableSchemas(overwriteAll);
        return db && tables;
    }

    /**
     * Creates the database for this project.
     *
     * @param overwrite If {@code true}, recreates the entire database.
     * @return {@code true} if no error was encountered.
     */
    public static boolean createDatabaseSchema(boolean overwrite) {
        try (var connection = getServerConnection()) {
            if (overwrite) {
                dropDatabase();
            }

            createDatabase(connection);

            return true;
        } catch (SQLException e) {
            LOGGER.error("Error creating database environment", e);
            return false;
        }
    }

    /**
     * Creates the tables for the database.
     *
     * @param overwrite If {@code true}, recreates the tables.
     * @return {@code true} if no error was encountered.
     */
    public static boolean createTableSchemas(boolean overwrite) {
        try (var pool = DatabaseConnectionPoolV2.getInstance()) {
            pool.execStmt(connection -> {
                if (overwrite) {
                    dropTable(connection, NAME_USER_CREDENTIALS);
                    dropTable(connection, NAME_BOOK);
                }

                createTable(connection, SCHEMA_USER_CREDENTIALS);
                createTable(connection, SCHEMA_BOOK);

                return null;
            });

            return true;
        } catch (SQLException e) {
            LOGGER.error("Error creating tables", e);
            return false;
        }
    }

    /**
     * Creates a database using a given connection.
     *
     * @param connection A connection to the SQL server.
     * @throws SQLException if an exception occurred during the SQL transaction.
     */
    private static void createDatabase(@NotNull final Connection connection) throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE DATABASE " + DB_NAME);
        }
    }

    /**
     * Creates a table in a database.
     *
     * @param connection A connection to a SQL database.
     * @param tableSpec The specification of the table.
     * @throws SQLException if an exception occurred during the SQL transaction.
     */
    private static void createTable(
            @NotNull final Connection connection,
            @Language(value = "SQL", prefix = "create table ") @NotNull final String tableSpec) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE " + tableSpec);
        }
    }

    /**
     * Drops a table from the database.
     *
     * @param connection A connection to a SQL database.
     * @param tableName The name of the table to drop.
     * @throws SQLException if an exception occurred during the SQL transaction.
     */
    private static void dropTable(@NotNull Connection connection, @NotNull String tableName) throws SQLException {
        try (var stmt = connection.prepareCall("DROP TABLE IF EXISTS " + tableName)) {
            stmt.execute();
        } catch (SQLException e) {
            LOGGER.error("Unable to drop table " + tableName, e);
            throw e;
        }
    }

    /**
     * Drops a database.
     *
     * @throws SQLException if an exception occurred during the SQL transaction.
     */
    public static void dropDatabase() throws SQLException {
        try (var connection = getServerConnection()) {
            try (var stmt = connection.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + DB_NAME);
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to drop database", e);
            throw e;
        }
    }

    /**
     * Sets the lock timeout for a database connection.
     *
     * @param con Connection to set the timeout.
     * @param timeout New timeout for lock contentions.
     */
    static void setLockTimeout(@NotNull final Connection con, @NotNull final Duration timeout) {
        try (var stmt = con.prepareStatement("SET SESSION innodb_lock_wait_timeout = ?")) {
            stmt.setInt(1, (int) timeout.toSeconds());
            stmt.execute();
        } catch (SQLException e) {
            LOGGER.warn("Cannot set database transaction timeout", e);
        }
    }

    /**
     * Retrieves the timeout of awaiting for locks.
     *
     * @param con Connection to get the lock timeout.
     * @return The duration which transactions wait for a lock before timing out.
     */
    @NotNull
    static Duration getLockTimeout(@NotNull final Connection con) {
        try (var stmt = con.prepareCall("SELECT @@innodb_lock_wait_timeout")) {
            final var rs = stmt.executeQuery();
            if (rs.next()) {
                return Duration.ofSeconds(rs.getInt(1));
            }

            LOGGER.warn("Cannot get database transaction timeout: Using defaults");
        } catch (SQLException e) {
            LOGGER.warn("Cannot get database transaction timeout: Using defaults", e);
        }
        return Duration.ofSeconds(50);
    }

    /**
     * Creates the set of default users in the database.
     */
    public static void createDefaultUsers() {
        if (!SecurityUtils.userLogin("user00001", "pass00001")) { // The database probably does not contain user credentials.
            try {
                DatabaseConnectionPoolV2.getInstance().execStmt(connection -> {
                    for (int i = 1; i <= 10000; ++i) {
                        String suffix = String.format("%05d", i);
                        LoginDataAccess.createUserAccount(connection, "user" + suffix, "pass" + suffix);
                    }

                    LOGGER.info("The user accounts are recreated");
                    return null;
                });
            } catch (SQLException e) {
                LOGGER.error("Unable to recreate user accounts", e);
            }
        }
    }
}
