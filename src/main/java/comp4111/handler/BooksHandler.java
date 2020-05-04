package comp4111.handler;

import comp4111.handler.impl.BooksHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Path handler for all {@code /books} requests.
 */
public abstract class BooksHandler extends HttpAsyncPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/books";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN + "*";
        }
    };

    @NotNull
    public static BooksHandler getInstance() {
        return new BooksHandlerImpl();
    }

    protected BooksHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    /**
     * Retrieves the book ID from the HTTP path.
     *
     * @param path Path of the HTTP request, as retrieved by {@link ClassicHttpRequest#getPath()}.
     * @return The ID of the book.
     */
    static long getIdFromRequest(@NotNull String path, @NotNull ResponseTrigger responseTrigger,
                                 @NotNull HttpContext context) throws IOException, HttpException {
        final var startIdx = (HANDLE_PATTERN + "/").length();
        final var endIdx = path.indexOf('?') != -1 ? path.indexOf('?') : path.length();

        try {
            return Long.parseLong(path.substring(startIdx, endIdx));
        } catch (NumberFormatException e) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException(e);
        }
    }

    static long getIdFromRequestWithoutException(@NotNull String path) {
        final var startIdx = (HANDLE_PATTERN + "/").length();
        final var endIdx = path.indexOf('?') != -1 ? path.indexOf('?') : path.length();

        try {
            return Long.parseLong(path.substring(startIdx, endIdx));
        } catch (Exception e) {
            return 0;
        }
    }
}
