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
 * Path handler for all {@code /books} requests.
 */
public final class BooksHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/books";

    /**
     * Lookup table for matching a method to its {@link HttpEndpointHandler}.
     */
    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            BooksGetHandler.getInstance(),
            BooksPostHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @NotNull
    public static BooksHandler getInstance() {
        return new BooksHandler();
    }

    private BooksHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return HANDLE_PATTERN;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(request, response, context, METHOD_LUT);
    }
}

