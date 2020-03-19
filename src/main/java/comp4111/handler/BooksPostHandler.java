package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.BooksPostHandlerImpl;
import comp4111.model.Book;
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

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private Book book;

    @NotNull
    public static BooksPostHandler getInstance() {
        return new BooksPostHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
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
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            throw new IllegalArgumentException();
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            throw new IllegalArgumentException();
        }

        if (request.getEntity() == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Payload must be specified", ContentType.TEXT_PLAIN));
            throw new IllegalArgumentException();
        }
        final var payload = request.getEntity().getContent().readAllBytes();

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
