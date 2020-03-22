package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BooksPostHandlerImpl;
import comp4111.model.Book;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /books} POST requests.
 */
public abstract class BooksPostHandler extends HttpEndpointHandler {

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

    protected Book book;

    @NotNull
    public static BooksPostHandler getInstance() {
        return new BooksPostHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = HttpUtils.parseQueryParams(request.getPath());
        final var token = checkToken(queryParams, response);

        final var payload = getPayload(request, response);

        try {
            book = objectMapper.readValue(payload, Book.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("POST /books token=\"{}\" Title=\"{}\" Author=\"{}\" Publisher=\"{}\" Year={}",
                token,
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear());
    }

    protected Book getBook() {
        return book;
    }
}
