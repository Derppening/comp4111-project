package comp4111.handler;

import comp4111.handler.impl.BooksDeleteHandlerImpl;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /book/*} DELETE requests.
 */
public abstract class BooksDeleteHandler extends HttpAsyncEndpointHandler {

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
    public void handle(Message<HttpRequest, String> requestObject,
                       AsyncServerRequestHandler.ResponseTrigger responseTrigger,
                       HttpContext context) throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final var queryParams = HttpUtils.parseQueryParams(requestObject.getHead().getPath(), responseTrigger, context);
        final var token = checkToken(queryParams, responseTrigger, context);

        bookId = BooksHandler.getIdFromRequest(requestObject.getHead().getPath(), responseTrigger, context);

        // TODO(Derppening): Consider shortcutting when bookId <= 0

        LOGGER.info("DELETE /books token=\"{}\" id={}", token, bookId);
    }

    protected long getBookId() {
        return bookId;
    }
}
