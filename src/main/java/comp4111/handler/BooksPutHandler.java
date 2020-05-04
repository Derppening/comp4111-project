package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BooksPutHandlerImpl;
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
 * Endpoint handler for all {@code /books/*} PUT requests.
 */
public abstract class BooksPutHandler extends HttpAsyncEndpointHandler {

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

    private long bookId;
    @Nullable
    private Boolean available;

    @NotNull
    public static BooksPutHandler getInstance() {
        return new BooksPutHandlerImpl();
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

        bookId = BooksHandler.getIdFromRequest(requestObject.getHead().getPath(), responseTrigger, context);

        final var payload = getPayload(requestObject, responseTrigger, context);

        try {
            final var rootNode = objectMapper.readTree(payload);
            final var node = rootNode.get("Available");
            if (!node.isBoolean()) {
                throw new IllegalArgumentException();
            }
            available = node.asBoolean();
        } catch (Exception e) {
            final AsyncResponseBuilder builder = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST);
            if (e.getLocalizedMessage() != null) {
                builder.setEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN);
            }
            final AsyncResponseProducer response = builder.build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("PUT /books token={} id={} Available={}", token, bookId, available);
    }

    protected long getBookId() {
        return bookId;
    }

    @NotNull
    protected Boolean getAvailable() {
        return Objects.requireNonNull(available);
    }
}
