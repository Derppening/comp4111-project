package comp4111.controller;

import comp4111.util.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manager for caching tokens.
 */
public class TokenManager {

    static final Supplier<Map<String, String>> DEFAULT_MAP_SUPPLIER = ConcurrentHashMap::new;

    @Nullable
    private static TokenManager INSTANCE;

    /**
     * @return The singleton instance of this class.
     */
    @NotNull
    public synchronized static TokenManager getInstance() {
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
    static synchronized TokenManager getInstance(@Nullable Map<String, String> backingMap) {
        if (INSTANCE == null) {
            final var map = backingMap != null ? backingMap : DEFAULT_MAP_SUPPLIER.get();

            INSTANCE = new TokenManager(map);
        }

        return INSTANCE;
    }

    private final Map<@NotNull String, @NotNull String> inFlightTokens;

    TokenManager(@NotNull Map<String, String> backingMap) {
        inFlightTokens = backingMap;
    }

    /**
     * Creates a new token.
     *
     * @param user The user requesting the token.
     * @return The token generated for the user, or {@code null} if a token is already generated for the user.
     */
    @Nullable
    public synchronized String newToken(@NotNull String user) {
        if (containsUser(user)) {
            return null;
        }

        final String token = SecurityUtils.generateRandomBase64String(24);
        inFlightTokens.put(token, user);
        return token;
    }

    /**
     * @return Whether the token is present.
     */
    public synchronized boolean containsToken(@NotNull String token) {
        return inFlightTokens.containsKey(token);
    }

    /**
     * @return Whether the user has generated a token.
     */
    public synchronized boolean containsUser(@NotNull String user) {
        return inFlightTokens.containsValue(user);
    }

    /**
     * Removes a token.
     *
     * @param token Token to remove.
     * @return {@code true} if the token was present in the cache and has been removed.
     */
    public synchronized boolean removeToken(@NotNull String token) {
        return inFlightTokens.remove(token) != null;
    }
}
