package comp4111.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manager for caching tokens.
 */
public class TokenManager {

    private static final Supplier<Map<String, String>> DEFAULT_MAP_SUPPLIER = ConcurrentHashMap::new;

    @Nullable
    private static TokenManager INSTANCE;

    /**
     * @return The singleton instance of this class.
     */
    @NotNull
    public static TokenManager getInstance() {
        return getInstance(null);
    }

    /**
     * This method is for cases where a custom {@link Map} class is required for backing the token map, such as for
     * mocking and testing.
     *
     * @param backingMap The map to use for storing tokens.
     * @return The singleton instance of this class.
     */
    @NotNull
    static TokenManager getInstance(@Nullable Map<String, String> backingMap) {
        synchronized (TokenManager.class) {
            if (INSTANCE == null) {
                final var map = backingMap != null ? backingMap : DEFAULT_MAP_SUPPLIER.get();

                INSTANCE = new TokenManager(map);
            }
        }

        return INSTANCE;
    }

    @NotNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @NotNull
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();

    private final Map<@NotNull String, @NotNull String> inFlightTokens;

    private TokenManager(@NotNull Map<String, String> backingMap) {
        inFlightTokens = backingMap;
    }

    /**
     * Creates a new token.
     *
     * @param user The user requesting the token.
     * @return The token generated for the user, or {@code null} if a token is already generated for the user.
     */
    @Nullable
    public String newToken(@NotNull String user) {
        final byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        final var token = BASE64_ENCODER.encodeToString(bytes);

        final var prevToken = inFlightTokens.putIfAbsent(token, user);

        return prevToken == null ? token : null;
    }

    /**
     * @return Whether the token is present.
     */
    public boolean containsToken(@NotNull String token) {
        return inFlightTokens.containsKey(token);
    }

    /**
     * @return Whether the user has generated a token.
     */
    public boolean containsUser(@NotNull String user) {
        return inFlightTokens.containsValue(user);
    }

    /**
     * Removes a token.
     *
     * @param token Token to remove.
     * @return {@code true} if the token was present in the cache and has been removed.
     */
    public boolean removeToken(@NotNull String token) {
        return inFlightTokens.remove(token) != null;
    }
}
