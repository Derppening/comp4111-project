package comp4111.handler;

import comp4111.handler.impl.BooksGetHandlerImpl;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /books} GET requests.
 */
public abstract class BooksGetHandler extends HttpAsyncEndpointHandler {

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

    @Nullable
    private Long queryId;
    @Nullable
    private String queryTitle;
    @Nullable
    private String queryAuthor;
    @Nullable
    private Integer queryLimit;
    @Nullable
    private String querySort;
    @Nullable
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
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final var queryParams = HttpUtils.parseQueryParams(requestObject.getHead().getPath(), responseTrigger, context);
        final var token = checkToken(queryParams, responseTrigger, context);

        // This handles the requests like GET /BookManagementService/books/1?token=FWb66_FtRZZWwRA0xnT9x06zhB6nBA93.
        long tempId = BooksHandler.getIdFromRequestWithoutException(requestObject.getHead().getPath());
        if (tempId > 0) {
            queryId = tempId;
        }

        final var queryIdStr = queryParams.getOrDefault("id", null);
        if (queryIdStr != null) {
            try {
                queryId = Long.parseLong(queryIdStr);
            } catch (NumberFormatException e) {
                final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
                responseTrigger.submitResponse(response, context);
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
                final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
                responseTrigger.submitResponse(response, context);
                throw new IllegalArgumentException(e);
            }
        }
        querySort = queryParams.getOrDefault("sortby", null);
        queryOrder = queryParams.getOrDefault("order", null);

        if (queryLimit != null && queryLimit < 0) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        if (queryOrder != null && !queryOrder.equals("asc") && !queryOrder.equals("desc")) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        if (querySort != null && !(querySort.equals("id") || querySort.equals("title") || querySort.equals("author") || querySort.equals("year"))) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        LOGGER.info("GET /books token=\"{}\" id={} title=\"{}\" author=\"{}\" limit={} sort={} order={}",
                token,
                queryId,
                queryTitle,
                queryAuthor,
                queryLimit,
                querySort,
                queryOrder);
    }

    @Nullable
    protected Long getQueryId() {
        return queryId;
    }

    @Nullable
    protected String getQueryTitle() {
        return queryTitle;
    }

    @Nullable
    protected String getQueryAuthor() {
        return queryAuthor;
    }

    @Nullable
    protected Integer getQueryLimit() {
        return queryLimit;
    }

    @Nullable
    protected String getQuerySort() {
        return querySort;
    }

    @Nullable
    protected String getQueryOrder() {
        return queryOrder;
    }
}
