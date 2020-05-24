package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.BooksPutHandlerImpl;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Endpoint handler for all {@code /books/*} PUT requests.
 */
public abstract class BooksPutHandler extends HttpAsyncEndpointHandler<BooksPutHandler.Request> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @Override
        public @NotNull String getHandlePattern() {
            return BooksHandler.HANDLE_PATTERN + "/*";
        }

        @Override
        public @NotNull Method getHandleMethod() {
            return Method.PUT;
        }
    };

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    public static class Request {

        public final long bookId;
        public final boolean available;


        public Request(long bookId, boolean available) {
            this.bookId = bookId;
            this.available = available;
        }
    }

    @Nullable
    private Request request;

    @NotNull
    public static BooksPutHandler getInstance() {
        return new BooksPutHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    protected CompletableFuture<Request> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(this::checkTokenAsync)
                .thenApplyAsync(HttpAsyncEndpointHandler::getPayloadAsync)
                .thenApplyAsync(payload -> {
                    try {
                        final var rootNode = objectMapper.readTree(payload);
                        final var node = rootNode.get("Available");
                        if (!node.isBoolean()) {
                            throw new IllegalArgumentException();
                        }
                        return node.asBoolean();
                    } catch (Exception e) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
                    }
                })
                .thenApplyAsync(isAvailable -> {
                    final var bookId = BooksHandler.getIdFromRequestAsync(requestObject.getHead().getPath());

                    request = new Request(bookId, isAvailable);
                    return request;
                })
                .thenApplyAsync(request -> {
                    LOGGER.info("PUT /books id={} Available={}", request.bookId, request.bookId);
                    return request;
                });
    }

    @NotNull
    public Request getRequest() {
        return Objects.requireNonNull(request);
    }

    @Deprecated(forRemoval = true)
    protected long getBookId() {
        return getRequest().bookId;
    }

    @NotNull
    @Deprecated(forRemoval = true)
    protected Boolean getAvailable() {
        return getRequest().available;
    }
}
