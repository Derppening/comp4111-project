package comp4111.util;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
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
    public static Map<String, String> parseQueryParams(@NotNull String path,
                                                       @NotNull AsyncServerRequestHandler.ResponseTrigger responseTrigger,
                                                       @NotNull HttpContext context) throws IOException, HttpException {
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
                final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
                responseTrigger.submitResponse(response, context);
                throw new IllegalArgumentException("Malformed query string");
            }
            final var chunkKey = queryChunk.substring(0, equalDelimiter);
            final var chunkVal = queryChunk.substring(equalDelimiter + 1);
            params.put(chunkKey, chunkVal);

            path = nextDelimiter != -1 ? path.substring(nextDelimiter + 1) : "";
        }
        return params;
    }

    /**
     * Retrieves the hostname of this server from a request.
     *
     * Implementation is referenced from {@link BasicHttpRequest#getUri()}.
     *
     * @param requestObject The HTTP request.
     * @return String representation of the server hostname.
     */
    @NotNull
    public static String getServerHostnameFromRequest(@NotNull Message<HttpRequest, String> requestObject) {
        final var buf = new StringBuilder();
        final HttpRequest request = requestObject.getHead();
        if (request.getAuthority() != null) {
            buf.append(request.getScheme() != null ? request.getScheme() : URIScheme.HTTP.id).append("://");
            buf.append(request.getAuthority().getHostName());
            if (request.getAuthority().getPort() >= 0) {
                buf.append(":").append(request.getAuthority().getPort());
            }
            if (request.getPath() == null) {
                buf.append("/");
            } else if (buf.length() > 0 && !request.getPath().startsWith("/")) {
                buf.append("/");
            }
        }
        return buf.toString();
    }

    /**
     * Retrieves the string representation of a request URI.
     *
     * This method does not convert the URI into a {@link java.net.URI} instance, and therefore does not throw
     * {@link java.net.URISyntaxException}.
     *
     * Implementation is referenced from {@link BasicHttpRequest#getUri()}.
     *
     * @param requestObject The HTTP request.
     * @return String representation of {@link BasicHttpRequest#getUri()}.
     */
    @NotNull
    public static String getRequestUriString(@NotNull Message<HttpRequest, String> requestObject) {
        final HttpRequest request = requestObject.getHead();
        try {
            return request.getUri().toString();
        } catch (URISyntaxException e) {
            final var buf = new StringBuilder();
            buf.append(getServerHostnameFromRequest(requestObject));
            if (request.getPath() != null) {
                buf.append(request.getPath());
            }

            return buf.toString();
        }
    }
}
