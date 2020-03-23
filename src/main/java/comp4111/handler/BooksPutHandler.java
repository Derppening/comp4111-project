package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BooksPutHandlerImpl;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /books/*} PUT requests.
 */
public abstract class BooksPutHandler extends HttpEndpointHandler {

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
    ;

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private long bookId;
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
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        checkMethod(request, response);

        final var queryParams = HttpUtils.parseQueryParams(request.getPath(), response);
        final var token = checkToken(queryParams, response);

        bookId = BooksHandler.getIdFromRequest(request.getPath(), response);

        final var payload = getPayload(request, response);

        try {
            final var rootNode = objectMapper.readTree(payload);
            available = rootNode.get("Available").asBoolean();
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("PUT /books token={} id={} Available={}", token, bookId, available);
    }

    protected long getBookId() {
        return bookId;
    }

    protected Boolean getAvailable() {
        return available;
    }
}
