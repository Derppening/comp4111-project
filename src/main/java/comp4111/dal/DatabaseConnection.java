package comp4111.dal;

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
    static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
    /**
     * The username used to login.
     */
    static final String MYSQL_LOGIN = "root";
    /**
     * The password of the user.
     */
    static final String MYSQL_PASSWORD = "comp4111";
    /**
     * The name of the database.
     */
    static final String DB_NAME = "comp4111";
    /**
     * The Connection object.
     */
    static Connection con;

    public static void setConfig() {
        // Create a connection to the MySQL server.
        try {
            // The connection is supposed to be closed in MainApplication.
            con = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            if (!databaseExists(con, DB_NAME)) {
                createDatabase(con, DB_NAME);
                useDatabase(con, DB_NAME);

                String tableSpec = "User_Credentials (" +
                        "    username varchar(40)," +
                        "    hashed_password varchar(64)," +
                        "    salt varchar(32)," +
                        "    primary key(username)" +
                        ");";
                createTable(con, tableSpec);

                tableSpec = "Book (" +
                        "    id int not null auto_increment," +
                        "    title varchar(80) unique," +
                        "    author varchar(80)," +
                        "    publisher varchar(80)," +
                        "    year int," +
                        "    available tinyint," +
                        "    primary key(id)" +
                        ");";
                createTable(con, tableSpec);
            }
        } catch (Exception e) {
            LOGGER.error("Error setting up the environment", e);
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL_URL + "/" + DB_NAME, MYSQL_LOGIN, MYSQL_PASSWORD);
    }

    public static void cleanUp() {
        try {
            con.close();
        } catch (SQLException e) {
            LOGGER.error("Error closing the connection", e);
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
        try (PreparedStatement stmt = con.prepareStatement("select schema_name from information_schema.schemata where schema_name = ?")) {
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
     * Creates a database with the given name.
     *
     * @param con {@link Connection} to the SQL server.
     * @param dbName Name of the new database.
     */
    private static void createDatabase(@NotNull final Connection con, @NotNull final String dbName) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_create_db.asp
            stmt.execute("create database " + dbName);
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
            stmt.execute("use " + dbName);
        }
    }

    /**
     * Creates a table on the database.
     *
     * @param con {@link Connection} to the database.
     * @param tableSpec Spec of the table. "CREATE TABLE" will be prepended.
     */
    private static void createTable(@NotNull final Connection con, @Language(value = "SQL", prefix = "create table ") @NotNull final String tableSpec) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_create_table.asp
            stmt.execute("create table " + tableSpec);
        }
    }
}
