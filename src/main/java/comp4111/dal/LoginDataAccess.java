package comp4111.dal;

import comp4111.dal.model.Credentials;
import comp4111.util.QueryUtils;
import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class LoginDataAccess extends Credentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDataAccess.class);

    public static void createUserAccount(
            @NotNull Connection connection,
            @NotNull String username,
            @NotNull String password) throws SQLException {
        final String salt = SecurityUtils.generateRandomBase64String(24);
        final String hashedPassword = SecurityUtils.calculateHash(password, salt, "SHA-256");
        Credentials c = new Credentials(username, hashedPassword, salt);

        try (var stmt = connection.prepareStatement("INSERT INTO User_Credentials VALUES(?, ?, ?)")) {
            stmt.setString(1, c.getUsername());
            stmt.setString(2, c.getHashedPassword());
            stmt.setString(3, c.getSalt());
            stmt.execute();
        }
    }

    /**
     * @return 1. the hashed password and 2. the salt.
     */
    @Nullable
    public static String[] getHashedPwdAndSalt(@NotNull String username) {
        // Create a connection to a specific database in the MySQL server.
        try {
            String[] result = new String[2];
            final var credentialsInDb = QueryUtils.queryTable(
                    null,
                    "User_Credentials",
                    InnoDBLockMode.SHARE.asSQLQueryComponent(),
                    new ArrayList<>(),
                    Credentials::toCredentials)
                    .get();
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
        } catch (Exception e) {
            LOGGER.error("Error querying the table", e);
        }
        return null;
    }
}
