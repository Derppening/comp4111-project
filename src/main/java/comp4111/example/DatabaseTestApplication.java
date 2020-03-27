package comp4111.example;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DatabaseTestApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseTestApplication.class);

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
    private static final String DB_NAME = "example";
    /**
     * The name of the dummy table.
     */
    private static final String TABLE_NAME = "example";

    /**
     * Sample class for database storage.
     */
    private static class Person {

        private Integer id = null;
        private String name;
        private int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public boolean hasId() {
            return id != null;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        /**
         * Converts this object to a SQL statement for insertion into a table.
         *
         * @param con Connection object to create the {@link PreparedStatement}.
         * @param tableName Table name to insert the statement into.
         * @return A {@link Statement} filled with the required information, ready for {@link PreparedStatement#execute())}.
         */
        public PreparedStatement toSQLStmt(final Connection con, final String tableName) throws SQLException {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tableName + "() VALUES (NULL, ?, ?)");
            stmt.setString(1, this.name);
            stmt.setInt(2, this.age);
            return stmt;
        }

        /**
         * Creates a {@link Person} object from a database row.
         *
         * @param rs {@link ResultSet} from the query.
         * @return An object representing the record, with the ID.
         */
        @NotNull
        static Person from(@NotNull ResultSet rs) {
            try {
                assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

                final var person = new Person(rs.getString(2), rs.getInt(3));
                person.id = rs.getInt(1);
                return person;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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

    /**
     * Queries all entries of a table, converting them into Java objects.
     *
     * @param con {@link Connection} to the database.
     * @param tableName Name of the table to query.
     * @param transform Transformation function to convert a {@link ResultSet} row into a Java object.
     * @param <T> Type of the object in Java.
     * @return {@link List} of rows, converted into Java objects.
     */
    private static <T> List<T> queryTable(@NotNull final Connection con, @NotNull String tableName, @NotNull Function<ResultSet, T> transform) throws SQLException {
        final var list = new ArrayList<T>();
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_select.asp
            final var rs = stmt.executeQuery("SELECT * FROM " + tableName);
            while (rs.next()) {
                list.add(transform.apply(rs));
            }
        }
        return list;
    }

    public static void main(String[] args) {

        // Create a connection to the MySQL server.
        try (Connection con = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD)) {
            if (databaseExists(con, DB_NAME)) {
                dropDatabase(con, DB_NAME);
            }

            createDatabase(con, DB_NAME);
            useDatabase(con, DB_NAME);
            createTable(con, TABLE_NAME + "(id int NOT NULL AUTO_INCREMENT, name varchar(40), age int(3), PRIMARY KEY (id))");
        } catch (Exception e) {
            LOGGER.error("Error setting up test environment", e);
            System.exit(1);
        }

        // Create a connection to a specific database in the MySQL server.
        try (Connection con = DriverManager.getConnection(MYSQL_URL + "/" + DB_NAME, MYSQL_LOGIN, MYSQL_PASSWORD)) {

            final var p = new Person("David", 20);
            try (PreparedStatement stmt = p.toSQLStmt(con, TABLE_NAME)) {
                stmt.execute();
            }

            final var personsInDb = queryTable(con, TABLE_NAME, Person::from);
            personsInDb.forEach(person -> System.out.println(person.getId() + "  " + person.getName() + "  " + person.getAge()));
        } catch (Exception e) {
            LOGGER.error("Error running test environment", e);
            System.exit(2);
        }
    }
}
