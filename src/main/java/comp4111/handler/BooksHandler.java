package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.controller.TokenManager;
import comp4111.model.Book;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
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
            new BooksGetHandler(),
            new BooksPostHandler()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

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
}

/**
 * Endpoint handler for all {@code /books} GET requests.
 */
final class BooksGetHandler extends HttpEndpointHandler {

    private final TokenManager tokenMgr = TokenManager.getInstance();

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @Override
            public @NotNull String getHandlePattern() {
                return BooksHandler.HANDLE_PATTERN;
            }

            @Override
            public @NotNull Method getHandleMethod() {
                return Method.GET;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        final var queryId = queryParams.getOrDefault("id", null);
        final var queryTitle = queryParams.getOrDefault("title", null);
        final var queryAuthor = queryParams.getOrDefault("author", null);
        final var queryLimit = queryParams.getOrDefault("limit", null);
        // TODO: sort+order must be specified together?
        final var querySort = queryParams.getOrDefault("sortby", null);
        final var queryOrder = queryParams.getOrDefault("order", null);

        LOGGER.info("POST /books token=\"{}\"", token);

        // TODO(Derppening): Handle QUERY operation on db

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}

/**
 * Endpoint handler for all {@code /books} POST requests.
 */
final class BooksPostHandler extends HttpEndpointHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenMgr = TokenManager.getInstance();

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return BooksHandler.HANDLE_PATTERN;
            }

            @NotNull
            @Override
            public Method getHandleMethod() {
                return Method.POST;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (request.getEntity() == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Payload must be specified", ContentType.TEXT_PLAIN));
            return;
        }
        final var payload = request.getEntity().getContent().readAllBytes();

        final Book book;
        try {
            book = objectMapper.readValue(payload, Book.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            return;
        }

        LOGGER.info("POST /books token=\"{}\" Title=\"{}\" Author=\"{}\" Publisher=\"{}\" Year={}",
                token,
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear());

        // TODO(Derppening): Handle ADD operation on db

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}
