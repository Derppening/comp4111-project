package comp4111.util;

import comp4111.dal.LoginDataAccess;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {
    @NotNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @NotNull
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();
    @NotNull
    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

    /**
     * @return {@code true} if the login is successful.
     */
    public static boolean userLogin(@NotNull String username, @NotNull String password) {
        String[] list = LoginDataAccess.getHashedPwdAndSalt(username);
        if (list == null) {
            return false;
        }
        return list[0].equals(calculateHash(password, list[1], "SHA-256"));
    }

    /**
     * @param byteArrayLength The length of the byte array
     * @return A Base64 string
     */
    public static String generateRandomBase64String(@NotNull int byteArrayLength) {
        final byte[] bytes = new byte[byteArrayLength];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes);
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
