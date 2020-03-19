package comp4111.handler;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A handler which binds to a specific {@link HttpPath}.
 *
 * Since this class does not have an associated {@link Method} bound to the handler, this class should be used as either
 * a generic path handler for all methods, or a dispatcher to dispatch the request to a {@link HttpEndpointHandler}.
 */
public abstract class HttpPathHandler implements HttpRequestHandler, HttpPath {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * @return The lookup table for matching a request sent with {@link Method} to its corresponding
     * {@link HttpEndpointHandler}. Can be {@code null} to indicate that a lookup table is not applicable to this
     * handler, for example if all requests should be handled the same regardless of its method.
     */
    protected abstract Map<Method, HttpEndpointHandler> getMethodLut();

    /**
     * @return The handler definition, which may be any object which inherits from {@link HttpPath}.
     */
    @NotNull
    public abstract HttpPath getHandlerDefinition();

    /**
     * @return The path pattern that this class handles.
     */
    @NotNull
    @Override
    public final String getHandlePattern() {
        return getHandlerDefinition().getHandlePattern();
    }

    /**
     * Parses a string into an HTTP method.
     *
     * @param methodStr String representation of the HTTP method.
     * @return Parsed {@link Method}, or {@code null} if the method is not known.
     */
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

    /**
     * Parses the query parameters of an HTTP request.
     *
     * @param path Path of the HTTP request.
     * @return {@link Map} of the query key-value pairs.
     */
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

    /**
     * Dispatches a request by its method.
     *
     * @param request {@link ClassicHttpRequest} to dispatch. Usually the first argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param response {@link ClassicHttpResponse} of the request. Usually the second argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param context {@link HttpContext} of the request. Usually the third argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param lut Lookup table for matching a {@link Method} to its corresponding {@link HttpEndpointHandler}.
     */
    protected static void dispatchByMethod(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context, Map<Method, HttpEndpointHandler> lut) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());

        HttpEndpointHandler handler = null;
        if (method != null) {
            handler = lut.get(method);
        }

        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", lut.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }
}
