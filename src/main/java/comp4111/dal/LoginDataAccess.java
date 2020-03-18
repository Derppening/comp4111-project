package comp4111.dal;

import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LoginDataAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDataAccess.class);

    private static class Credentials {
        private String username;
        private String hashedPassword;
        private String salt;

        private Credentials(@NotNull String username, @NotNull String hashedPassword, @NotNull String salt) {
            this.username = username;
            this.hashedPassword = hashedPassword;
            this.salt = salt;
        }

        /**
         * Converts this object to a SQL statement for insertion into a table.
         *
         * @param con Connection object to create the {@link PreparedStatement}.
         * @param tableName Table name to insert the statement into.
         * @return A {@link Statement} filled with the required information, ready for {@link PreparedStatement#execute())}.
         */
        private PreparedStatement toSQLStmt(@NotNull final Connection con, @NotNull final String tableName) throws SQLException {
            PreparedStatement stmt = con.prepareStatement("insert into " + tableName + " values(?, ?, ?)");
            stmt.setString(1, this.username);
            stmt.setString(2, this.hashedPassword);
            stmt.setString(3, this.salt);
            return stmt;
        }

        /**
         * Creates a {@link Credentials} object from a database row.
         *
         * @param rs {@link ResultSet} from the query.
         * @return An object representing the record, with the ID.
         */
        @NotNull
        static Credentials from(@NotNull ResultSet rs) {
            try {
                assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

                final Credentials c = new Credentials(rs.getString(1), rs.getString(2), rs.getString(3));
                return c;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void createUserAccount(@NotNull final String username, @NotNull final String password) {
        final String salt = SecurityUtils.generateRandomBase64String(24);
        final String hashedPassword = SecurityUtils.calculateHash(password, salt, "SHA-256");
        Credentials c = new Credentials(username, hashedPassword, salt);

        try (PreparedStatement stmt = c.toSQLStmt(DatabaseConnection.con, "User_Credentials")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 1. the hashed password and 2. the salt.
     */
    public static String[] getHashedPwdAndSalt(@NotNull String username) {
        // Create a connection to a specific database in the MySQL server.
        try (Connection con = DriverManager.getConnection(DatabaseConnection.MYSQL_URL + "/" + DatabaseConnection.DB_NAME,
                DatabaseConnection.MYSQL_LOGIN, DatabaseConnection.MYSQL_PASSWORD)) {
            String[] result = new String[2];
            final var credentialsInDb = queryTable(con, "User_Credentials", Credentials::from);
            credentialsInDb.forEach(c -> {
                if (c.username.equals(username)) {
                    // There should only be one set.
                    result[0] = c.hashedPassword;
                    result[1] = c.salt;
                }
            });
            if (result[0] == null && result[1] == null) {
                return null;
            }

            return result;
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return null;
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
            final var rs = stmt.executeQuery("select * from " + tableName);
            while (rs.next()) {
                list.add(transform.apply(rs));
            }
        }
        return list;
    }
}
