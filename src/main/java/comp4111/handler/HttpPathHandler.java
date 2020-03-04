package comp4111.handler;

import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpPathHandler implements HttpRequestHandler, HttpPath {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @NotNull
    public abstract HttpPath getHandlerDefinition();

    @NotNull
    @Override
    public final String getHandlePattern() {
        return getHandlerDefinition().getHandlePattern();
    }

    @Nullable
    protected static Method toMethodOrNull(@NotNull String methodStr) {
        Method method = null;
        try {
            method = Method.normalizedValueOf(methodStr);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        return method;
    }

    @NotNull
    protected static Map<String, String> parseQueryParams(@NotNull String path) {
        final var queryStartIndex = path.indexOf('?');
        if (queryStartIndex == -1) {
            return Map.of();
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
