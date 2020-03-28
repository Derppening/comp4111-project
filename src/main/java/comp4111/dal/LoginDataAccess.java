package comp4111.dal;

import comp4111.dal.model.Credentials;
import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LoginDataAccess extends Credentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDataAccess.class);

    public static void createUserAccount(@NotNull final String username, @NotNull final String password) {
        final String salt = SecurityUtils.generateRandomBase64String(24);
        final String hashedPassword = SecurityUtils.calculateHash(password, salt, "SHA-256");
        Credentials c = new Credentials(username, hashedPassword, salt);

        try (
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement("insert into User_Credentials values(?, ?, ?)")
        ) {
            stmt.setString(1, c.getUsername());
            stmt.setString(2, c.getHashedPassword());
            stmt.setString(3, c.getSalt());
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
        try (Connection con = DatabaseConnection.getConnection()) {
            String[] result = new String[2];
            final var credentialsInDb = queryTable(con, "User_Credentials", Credentials::toCredentials);
            credentialsInDb.forEach(c -> {
                if (c.getUsername().equals(username)) {
                    // There should only be one set.
                    result[0] = c.getHashedPassword();
                    result[1] = c.getSalt();
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
