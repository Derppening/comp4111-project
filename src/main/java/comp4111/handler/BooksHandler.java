package comp4111.handler;

import comp4111.handler.impl.BooksHandlerImpl;
import org.jetbrains.annotations.NotNull;

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
}

