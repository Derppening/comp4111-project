package comp4111.handler;

import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.BooksGetHandlerImpl;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Endpoint handler for all {@code /books} GET requests.
 */
public abstract class BooksGetHandler extends HttpAsyncEndpointHandler<BooksGetHandler.QueryParams> {

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

    public static class QueryParams {

        public enum SortField {
            NONE, ID, TITLE, AUTHOR, YEAR;

            @NotNull
            public String toSQLComponent() {
                return this.toString().toLowerCase();
            }

            @NotNull
            public static SortField normalizedValueOf(@Nullable String value) {
                return value != null ? SortField.valueOf(value.toUpperCase()) : SortField.NONE;
            }
        }

        public enum OutputOrder {
            NONE, ASC, DESC;

            @NotNull
            public String toSQLComponent() {
                return this.toString().toLowerCase();
            }

            @NotNull
            public static OutputOrder normalizedValueOf(@Nullable String value) {
                return value != null ? OutputOrder.valueOf(value.toUpperCase()) : OutputOrder.NONE;
            }
        }

        @Nullable
        public final Long id;
        @Nullable
        public final String title;
        @Nullable
        public final String author;
        @Nullable
        public final Integer limit;
        @NotNull
        public final SortField sort;
        @NotNull
        public final OutputOrder order;

        private QueryParams(
                @Nullable Long id,
                @Nullable String title,
                @Nullable String author,
                @Nullable Integer limit,
                @NotNull SortField sort,
                @NotNull OutputOrder order) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.limit = limit;
            this.sort = sort;
            this.order = order;
        }
    }

    @Nullable
    private QueryParams queryParams;

    @NotNull
    public static BooksGetHandler getInstance() {
        return new BooksGetHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    protected CompletableFuture<QueryParams> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(this::checkTokenAsync)
                .thenApplyAsync(request -> HttpUtils.parseQueryParamsAsync(request.getHead().getPath()))
                .thenApplyAsync(params -> {
                    try {
                        long queryId = BooksHandler.getIdFromRequestAsync(requestObject.getHead().getPath());

                        if (queryId <= 0) {
                            throw new CompletionException("<0 path id", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
                        }

                        queryParams = new QueryParams(
                                queryId,
                                null,
                                null,
                                null,
                                QueryParams.SortField.NONE,
                                QueryParams.OutputOrder.NONE
                        );
                    } catch (Exception e) {
                        queryParams = parseQueryParams(params);
                    }
                    return queryParams;
                })
                .thenApplyAsync(queryParams -> {
                    LOGGER.info("GET /books id={} title=\"{}\" author=\"{}\" limit={} sort={} order={}",
                            queryParams.id,
                            queryParams.title,
                            queryParams.author,
                            queryParams.limit,
                            queryParams.sort,
                            queryParams.order);
                    return queryParams;
                });
    }

    @NotNull
    private QueryParams parseQueryParams(@NotNull Map<String, String> params) {
        Long queryId;
        final var queryIdStr = params.getOrDefault("id", null);
        try {
            queryId = queryIdStr != null ? Long.parseLong(queryIdStr) : null;
        } catch (NumberFormatException e) {
            throw new CompletionException("Bad id value", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
        }

        final var queryTitle = params.getOrDefault("title", null);
        final var queryAuthor = params.getOrDefault("author", null);

        Integer queryLimit;
        final var queryLimitStr = params.getOrDefault("limit", null);
        try {
            queryLimit = queryLimitStr != null ? Integer.parseInt(queryLimitStr) : null;
        } catch (NumberFormatException e) {
            throw new CompletionException("Bad limit value", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
        }

        QueryParams.SortField querySort;
        try {
            querySort = QueryParams.SortField.normalizedValueOf(params.getOrDefault("sortby", null));
        } catch (IllegalArgumentException e) {
            throw new CompletionException("Bad sortby value", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
        }

        QueryParams.OutputOrder queryOrder;
        try {
            queryOrder = QueryParams.OutputOrder.normalizedValueOf(params.getOrDefault("order", null));
        } catch (IllegalArgumentException e) {
            throw new CompletionException("Bad order value", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
        }

        if (queryLimit != null && queryLimit < 0) {
            throw new CompletionException("<0 limit value", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
        }

        return new QueryParams(queryId, queryTitle, queryAuthor, queryLimit, querySort, queryOrder);
    }

    @NotNull
    QueryParams getQueryParams() {
        return Objects.requireNonNull(queryParams);
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected Long getQueryId() {
        return getQueryParams().id;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected String getQueryTitle() {
        return getQueryParams().title;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected String getQueryAuthor() {
        return getQueryParams().author;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected Integer getQueryLimit() {
        return getQueryParams().limit;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected String getQuerySort() {
        return getQueryParams().sort.toSQLComponent();
    }

    @Nullable
    @Deprecated(forRemoval = true)
    protected String getQueryOrder() {
        return getQueryParams().order.toSQLComponent();
    }
}
