package comp4111.util;

import comp4111.dal.LoginDataAccess;
import org.jetbrains.annotations.NotNull;

public class LoginUtils {
    /**
     * @return {@code true} if the login is successful.
     */
    public static boolean userLogin(@NotNull String username, @NotNull String password) {
        String[] list = LoginDataAccess.getHashedPwdAndSalt(username);
        if (list == null) {
            return false;
        }
        return list[0].equals(SecurityUtils.calculateHash(password, list[1], "SHA-256"));
    }
}
