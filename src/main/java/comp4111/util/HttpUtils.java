package comp4111.util;

import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Parses a string into an HTTP method.
     *
     * @param methodStr String representation of the HTTP method.
     * @return Parsed {@link Method}, or {@code null} if the method is not known.
     */
    @Nullable
    public static Method toMethodOrNull(@NotNull String methodStr) {
        Method method = null;
        try {
            method = Method.normalizedValueOf(methodStr);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        return method;
    }

    /**
     * Parses the query parameters of an HTTP request.
     *
     * @param path Path of the HTTP request.
     * @return {@link Map} of the query key-value pairs.
     */
    @NotNull
    public static Map<String, String> parseQueryParams(@NotNull String path) {
        final var queryStartIndex = path.indexOf('?');
        if (queryStartIndex == -1) {
            return Collections.emptyMap();
        }

        path = path.substring(queryStartIndex + 1);

        final var params = new HashMap<String, String>();
        while (!path.isEmpty()) {
            final var nextDelimiter = path.indexOf('&');
            final var queryChunk = nextDelimiter != -1 ? path.substring(0, nextDelimiter) : path;

            final var equalDelimiter = queryChunk.indexOf('=');
            if (equalDelimiter == -1) {
                throw new IllegalArgumentException("Malformed query string");
            }
            final var chunkKey = queryChunk.substring(0, equalDelimiter);
            final var chunkVal = queryChunk.substring(equalDelimiter + 1);
            params.put(chunkKey, chunkVal);

            path = nextDelimiter != -1 ? path.substring(nextDelimiter + 1) : "";
        }
        return params;
    }
}
