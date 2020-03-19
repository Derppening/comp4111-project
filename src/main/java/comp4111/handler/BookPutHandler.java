package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BookPutHandlerImpl;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /book/*} PUT requests.
 */
public abstract class BookPutHandler extends HttpEndpointHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private int bookId;
    private Boolean available;

    @NotNull
    public static BookPutHandler getInstance() {
        return new BookPutHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @Override
            public @NotNull String getHandlePattern() {
                return BookHandler.HANDLE_PATTERN;
            }

            @Override
            public @NotNull Method getHandleMethod() {
                return Method.PUT;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        final var token = checkToken(queryParams, response);

        final var bookId = BookHandler.getIdFromRequest(request.getPath());

        final var payload = getPayload(request, response);

        final boolean available;
        try {
            final var rootNode = objectMapper.readTree(payload);
            available = rootNode.get("Available").asBoolean();
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("PUT /book token={} id={} Available={}", token, bookId, available);
    }

    protected int getBookId() {
        return bookId;
    }

    protected Boolean getAvailable() {
        return available;
    }
}
