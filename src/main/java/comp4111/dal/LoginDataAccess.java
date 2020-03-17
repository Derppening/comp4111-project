package comp4111.dal;

import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LoginDataAccess {
    @NotNull
    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

    public static void createUserAccount(@NotNull final String username, @NotNull final String password) {
        final String salt = SecurityUtils.generateRandomBase64String(24);
        final String hashedPassword = calculateHash(password, salt, "SHA-256");
    }

    /**
     * https://javainterviewpoint.com/java-salted-password-hashing/
     */
    public static String calculateHash(@NotNull final String password, @NotNull final String saltString, @NotNull final String algorithm) {
        MessageDigest md;
        try {
            // Select the message digest for the hash computation
            md = MessageDigest.getInstance(algorithm);

            final byte[] salt = BASE64_DECODER.decode(saltString);

            // Pass the salt to the digest for the computation
            md.update(salt);

            // Generate the salted hash
            final byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            final StringBuilder sb = new StringBuilder();
            for (final byte b : hashedPassword)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
