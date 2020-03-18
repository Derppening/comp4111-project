package comp4111.handler;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Path handler for all {@code /book/*} requests.
 */
public final class BookHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/book/";

    /**
     * Lookup table for matching a method to its {@link HttpEndpointHandler}.
     */
    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            BookPutHandler.getInstance(),
            BookDeleteHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @NotNull
    public static BookHandler getInstance() {
        return new BookHandler();
    }

    private BookHandler() {
    }

    @NotNull
    @Override
    public HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return HANDLE_PATTERN + "*";
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());

        HttpEndpointHandler handler = null;
        if (method != null) {
            handler = METHOD_LUT.get(method);
        }

        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", METHOD_LUT.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }

    /**
     * Retrieves the book ID from the HTTP path.
     *
     * @param path Path of the HTTP request, as retrieved by {@link ClassicHttpRequest#getPath()}.
     * @return The ID of the book.
     */
    static long getIdFromRequest(@NotNull String path) {
        final var startIdx = HANDLE_PATTERN.length();
        final var endIdx = path.indexOf('?') != -1 ? path.indexOf('?') : path.length();

        return Long.parseLong(path.substring(startIdx, endIdx));
    }
}

