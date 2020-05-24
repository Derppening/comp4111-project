package comp4111.handler;

import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.BooksDeleteHandlerImpl;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Endpoint handler for all {@code /book/*} DELETE requests.
 */
public abstract class BooksDeleteHandler extends HttpAsyncEndpointHandler<Long> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @NotNull
        @Override
        public String getHandlePattern() {
            return BooksHandler.HANDLE_PATTERN + "/*";
        }

        @NotNull
        @Override
        public Method getHandleMethod() {
            return Method.DELETE;
        }
    };

    private long bookId;

    @NotNull
    public static BooksDeleteHandler getInstance() {
        return new BooksDeleteHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    protected CompletableFuture<Long> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(this::checkTokenAsync)
                .thenApplyAsync(request -> BooksHandler.getIdFromRequestAsync(request.getHead().getPath()))
                .thenApplyAsync(id -> {
                    if (id <= 0) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
                    }

                    bookId = id;
                    return bookId;
                })
                .thenApplyAsync(id -> {
                    LOGGER.info("DELETE /books id={}", id);
                    return id;
                });
    }

    long getBookId() {
        return bookId;
    }
}
