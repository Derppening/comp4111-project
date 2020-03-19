package comp4111.handler;

import comp4111.handler.impl.BooksHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Path handler for all {@code /books} requests.
 */
public abstract class BooksHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/books";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN;
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

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(request, response, context, getMethodLut());
    }
}

