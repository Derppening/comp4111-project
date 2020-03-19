package comp4111.handler;

import comp4111.handler.impl.BookHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Path handler for all {@code /book/*} requests.
 */
public abstract class BookHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/book/";

    @NotNull
    public static BookHandler getInstance() {
        return new BookHandlerImpl();
    }

    protected BookHandler() {
    }

    @NotNull
    @Override
    public HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return HANDLE_PATTERN + "*";
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(request, response, context, getMethodLut());
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

