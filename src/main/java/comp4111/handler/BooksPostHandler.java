package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.BooksPostHandlerImpl;
import comp4111.model.Book;
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
 * Endpoint handler for all {@code /books} POST requests.
 */
public abstract class BooksPostHandler extends HttpAsyncEndpointHandler<BooksPostHandler.Request> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
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

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    public static class Request {

        @NotNull
        public final String token;

        @NotNull
        public final Book book;


        public Request(@NotNull String token, @NotNull Book book) {
            this.token = token;
            this.book = book;
        }
    }

    @Nullable
    private Request request;

    @NotNull
    public static BooksPostHandler getInstance() {
        return new BooksPostHandlerImpl();
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
                    final var token = getTokenAsync(requestObject);

                    final Book book;
                    try {
                        book = objectMapper.readValue(payload, Book.class);
                    } catch (Exception e) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
                    }

                    request = new Request(token, book);
                    return request;
                })
                .thenApplyAsync(request -> {
                    LOGGER.info("POST /books token=\"{}\" Title=\"{}\" Author=\"{}\" Publisher=\"{}\" Year={}",
                            request.token,
                            request.book.getTitle(),
                            request.book.getAuthor(),
                            request.book.getPublisher(),
                            request.book.getYear());
                    return request;
                });
    }

    @NotNull
    Request getRequest() {
        return Objects.requireNonNull(request);
    }

    @NotNull
    @Deprecated(forRemoval = true)
    protected String getToken() {
        return getRequest().token;
    }

    @NotNull
    @Deprecated(forRemoval = true)
    protected Book getBook() {
        return getRequest().book;
    }
}
