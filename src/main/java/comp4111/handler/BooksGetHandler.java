package comp4111.handler;

import comp4111.handler.impl.BooksGetHandlerImpl;
import comp4111.util.HttpUtils;
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

    private Long queryId;
    private String queryTitle;
    private String queryAuthor;
    private Integer queryLimit;
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
        checkMethod(request, response);

        final var queryParams = HttpUtils.parseQueryParams(request.getPath(), response);
        final var token = checkToken(queryParams, response);

        // This handles the requests like GET /BookManagementService/books/1?token=FWb66_FtRZZWwRA0xnT9x06zhB6nBA93.
        long tempId = BooksHandler.getIdFromRequestWithoutException(request.getPath(), response);
        if (tempId > 0) {
            queryId = tempId;
        }

        final var queryIdStr = queryParams.getOrDefault("id", null);
        if (queryIdStr != null) {
            try {
                queryId = Long.parseLong(queryIdStr);
            } catch (NumberFormatException e) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                throw new IllegalArgumentException(e);
            }
        }
        queryTitle = queryParams.getOrDefault("title", null);
        queryAuthor = queryParams.getOrDefault("author", null);
        final var queryLimitStr = queryParams.getOrDefault("limit", null);
        if (queryLimitStr != null) {
            try {
                queryLimit = Integer.parseInt(queryLimitStr);
            } catch (NumberFormatException e) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                throw new IllegalArgumentException(e);
            }
        }
        querySort = queryParams.getOrDefault("sortby", null);
        queryOrder = queryParams.getOrDefault("order", null);

        // TODO(Derppening): Early check whether limit, sortby and order parameters are correct

        LOGGER.info("POST /books token=\"{}\"", token);
    }

    protected Long getQueryId() {
        return queryId;
    }

    protected String getQueryTitle() {
        return queryTitle;
    }

    protected String getQueryAuthor() {
        return queryAuthor;
    }

    protected Integer getQueryLimit() {
        return queryLimit;
    }

    protected String getQuerySort() {
        return querySort;
    }

    protected String getQueryOrder() {
        return queryOrder;
    }
}
