package comp4111.handler;

import comp4111.handler.impl.BookDeleteHandlerImpl;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /book/*} DELETE requests.
 */
public abstract class BookDeleteHandler extends HttpEndpointHandler {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
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

    private int bookId;

    @NotNull
    public static BookDeleteHandler getInstance() {
        return new BookDeleteHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = HttpUtils.parseQueryParams(request.getPath());
        final var token = checkToken(queryParams, response);

        final var bookId = BookHandler.getIdFromRequest(request.getPath());

        LOGGER.info("DELETE /book token=\"{}\" id={}", token, bookId);
    }

    protected int getBookId() {
        return bookId;
    }
}
