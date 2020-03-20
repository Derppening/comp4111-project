package comp4111.handler;

import comp4111.handler.impl.BookHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.jetbrains.annotations.NotNull;

/**
 * Path handler for all {@code /book/*} requests.
 */
public abstract class BookHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/book/";

    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN + "*";
        }
    };

    @NotNull
    public static BookHandler getInstance() {
        return new BookHandlerImpl();
    }

    protected BookHandler() {
    }

    @NotNull
    @Override
    public HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    /**
     * Retrieves the book ID from the HTTP path.
     *
     * @param path Path of the HTTP request, as retrieved by {@link ClassicHttpRequest#getPath()}.
     * @return The ID of the book.
     */
    static long getIdFromRequest(@NotNull String path) {
        final var startIdx = HANDLE_PATTERN.length();
        final var endIdx = path.indexOf('?') != -1 ? path.indexOf('?') : path.length();

        return Long.parseLong(path.substring(startIdx, endIdx));
    }
}

