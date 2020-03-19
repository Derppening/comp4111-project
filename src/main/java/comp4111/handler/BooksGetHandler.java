package comp4111.handler;

import comp4111.handler.impl.BooksGetHandlerImpl;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /books} GET requests.
 */
public abstract class BooksGetHandler extends HttpEndpointHandler {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @Override
        public @NotNull String getHandlePattern() {
            return BooksHandler.HANDLE_PATTERN;
        }

        @Override
        public @NotNull Method getHandleMethod() {
            return Method.GET;
        }
    };

    private Integer queryId;
    private String queryTitle;
    private String queryAuthor;
    private String queryLimit;
    private String querySort;
    private String queryOrder;

    @NotNull
    public static BooksGetHandler getInstance() {
        return new BooksGetHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        final var token = checkToken(queryParams, response);

        final var queryIdStr = queryParams.getOrDefault("id", null);
        if (queryIdStr != null) {
            try {
                queryId = Integer.parseInt(queryIdStr);
            } catch (NumberFormatException e) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                throw new IllegalArgumentException(e);
            }
        }
        queryTitle = queryParams.getOrDefault("title", null);
        queryAuthor = queryParams.getOrDefault("author", null);
        queryLimit = queryParams.getOrDefault("limit", null);
        // TODO: sort+order must be specified together?
        querySort = queryParams.getOrDefault("sortby", null);
        queryOrder = queryParams.getOrDefault("order", null);

        LOGGER.info("POST /books token=\"{}\"", token);
    }

    protected Integer getQueryId() {
        return queryId;
    }

    protected String getQueryTitle() {
        return queryTitle;
    }

    protected String getQueryAuthor() {
        return queryAuthor;
    }

    protected String getQueryLimit() {
        return queryLimit;
    }

    protected String getQuerySort() {
        return querySort;
    }

    protected String getQueryOrder() {
        return queryOrder;
    }
}