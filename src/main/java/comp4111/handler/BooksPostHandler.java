package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BooksPostHandlerImpl;
import comp4111.model.Book;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * Endpoint handler for all {@code /books} POST requests.
 */
public abstract class BooksPostHandler extends HttpAsyncEndpointHandler {

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

    @Nullable
    private String token;

    @Nullable
    private Book book;

    @NotNull
    public static BooksPostHandler getInstance() {
        return new BooksPostHandlerImpl();
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
        token = checkToken(queryParams, responseTrigger, context);

        final var payload = getPayload(requestObject, responseTrigger, context);

        try {
            book = objectMapper.readValue(payload, Book.class);
        } catch (Exception e) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST)
                    .setEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("POST /books token=\"{}\" Title=\"{}\" Author=\"{}\" Publisher=\"{}\" Year={}",
                token,
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear());
    }

    @NotNull
    protected String getToken() {
        return Objects.requireNonNull(token);
    }

    @NotNull
    protected Book getBook() {
        return Objects.requireNonNull(book);
    }
}
