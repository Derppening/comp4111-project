package comp4111.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for caching tokens.
 */
public class TokenManager {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();

    private static final Map<@NotNull String, @NotNull String> IN_FLIGHT_TOKENS = new ConcurrentHashMap<>();

    /**
     * Creates a new token.
     *
     * @param user The user requesting the token.
     * @return The token generated for the user, or {@code null} if a token is already generated for the user.
     */
    @Nullable
    public static String newToken(@NotNull String user) {
        final byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        final var token = BASE64_ENCODER.encodeToString(bytes);

        final var prevToken = IN_FLIGHT_TOKENS.putIfAbsent(token, user);

        return prevToken == null ? token : null;
    }

    /**
     * @return Whether the token is present.
     */
    public static boolean containsToken(@NotNull String token) {
        return IN_FLIGHT_TOKENS.containsKey(token);
    }

    /**
     * @return Whether the user has generated a token.
     */
    public static boolean containsUser(@NotNull String user) {
        return IN_FLIGHT_TOKENS.containsValue(user);
    }

    /**
     * Removes a token.
     *
     * @param token Token to remove.
     * @return {@code true} if the token was present in the cache and has been removed.
     */
    public static boolean removeToken(@NotNull String token) {
        return IN_FLIGHT_TOKENS.remove(token) != null;
    }
}
