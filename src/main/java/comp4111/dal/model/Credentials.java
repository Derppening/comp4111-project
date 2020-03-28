package comp4111.dal.model;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Credentials {

    private String username;
    private String hashedPassword;
    private String salt;

    public Credentials() {
    }

    public Credentials(@NotNull String username, @NotNull String hashedPassword, @NotNull String salt) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }

    /**
     * Creates a {@link Credentials} object from a database row.
     *
     * @param rs {@link ResultSet} from the query.
     * @return An object representing the record.
     */
    @NotNull
    protected static Credentials toCredentials(@NotNull ResultSet rs) {
        try {
            assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

            return new Credentials(rs.getString(1), rs.getString(2), rs.getString(3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSalt() {
        return salt;
    }
}
