package comp4111.connection;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(comp4111.example.DatabaseTestApplication.class);

    /**
     * The URL to the MySQL database.
     */
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
    /**
     * The username used to login.
     */
    private static final String MYSQL_LOGIN = "root";
    /**
     * The password of the user.
     */
    private static final String MYSQL_PASSWORD = "comp4111";
    /**
     * The name of the database.
     */
    private static final String DB_NAME = "comp4111";

    public static void setConfig() {
        // Create a connection to the MySQL server.
        try (Connection con = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD)) {
            if (databaseExists(con, DB_NAME)) {
                dropDatabase(con, DB_NAME);
            }

            createDatabase(con, DB_NAME);
            useDatabase(con, DB_NAME);
        } catch (Exception e) {
            LOGGER.error("Error setting up the environment", e);
            System.exit(1);
        }
    }

    /**
     * Checks whether a database with the given name exists.
     *
     * @param con {@link Connection} to the SQL server.
     * @param dbName Name of the database to check.
     * @return {@code true} if it exists.
     */
    private static boolean databaseExists(@NotNull final Connection con, @NotNull final String dbName) throws SQLException {
        // https://stackoverflow.com/a/838993
        // PreparedStatement is used here to prevent SQL injection attacks
        // Use '?' to indicate a parameter, which can then be substituted later.
        try (PreparedStatement stmt = con.prepareStatement("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?")) {
            // The set* methods set the actual value of the parameters.
            // Note that the parameterIndex is 1-based.
            stmt.setString(1, dbName);

            // ResultSet represents the result of a query.
            ResultSet rs = stmt.executeQuery();

            // rs.first() checks whether there is a "first element" in the query.
            return rs.first();
        }
    }

    /**
     * Drops a database.
     *
     * @param con {@link Connection} to the SQL server.
     * @param dbName Name of the database to drop.
     */
    private static void dropDatabase(@NotNull final Connection con, @NotNull final String dbName) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_drop_db.asp
            // Be careful with this.
            stmt.execute("DROP DATABASE " + dbName);
        }
    }

    /**
     * Creates a database with the given name.
     *
     * @param con {@link Connection} to the SQL server.
     * @param dbName Name of the new database.
     */
    private static void createDatabase(@NotNull final Connection con, @NotNull final String dbName) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_create_db.asp
            stmt.execute("CREATE DATABASE " + dbName);
        }
    }

    /**
     * Specifies to use a particular database for subsequent statements.
     *
     * @param con {@link Connection} to the SQL server.
     * @param dbName Name of the database to use.
     */
    private static void useDatabase(@NotNull final Connection con, @NotNull final String dbName) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // Don't do this.
            // ...
            // If you have to do this, create a new connection which only connects to the specific database.
            stmt.execute("USE " + dbName);
        }
    }

    /**
     * Creates a table on the database.
     *
     * @param con {@link Connection} to the database.
     * @param tableSpec Spec of the table. "CREATE TABLE" will be prepended.
     */
    private static void createTable(@NotNull final Connection con, @Language(value = "SQL", prefix = "CREATE TABLE ") @NotNull final String tableSpec) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_create_table.asp
            stmt.execute("CREATE TABLE " + tableSpec);
        }
    }
}
