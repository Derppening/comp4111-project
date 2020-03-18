package comp4111.handler;

import comp4111.controller.TokenManager;
import comp4111.handler.impl.BookDeleteHandlerImpl;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /book/*} DELETE requests.
 */
public abstract class BookDeleteHandler extends HttpEndpointHandler {

    private final TokenManager tokenMgr = TokenManager.getInstance();

    private int bookId;

    @NotNull
    public static BookDeleteHandler getInstance() {
        return new BookDeleteHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return BookHandler.HANDLE_PATTERN;
            }

            @NotNull
            @Override
            public Method getHandleMethod() {
                return Method.DELETE;
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

        final var bookId = BookHandler.getIdFromRequest(request.getPath());

        LOGGER.info("DELETE /book token=\"{}\" id={}", token, bookId);
    }

    protected int getBookId() {
        return bookId;
    }
}
