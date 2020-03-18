package comp4111.dal;

import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginDataAccess {
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
}
