package comp4111.util;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {
    @NotNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @NotNull
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();

    /**
     * @param byteArrayLength The length of the byte array
     * @return A Base64 string
     */
    public static String generateRandomBase64String(@NotNull int byteArrayLength) {
        final byte[] bytes = new byte[byteArrayLength];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes);
    }
}
