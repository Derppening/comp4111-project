package comp4111.handler;

import comp4111.handler.impl.TransactionHandlerImpl;
import org.jetbrains.annotations.NotNull;

public abstract class TransactionHandler extends HttpAsyncPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/transaction";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @Override
        public @NotNull String getHandlePattern() {
            return HANDLE_PATTERN;
        }
    };

    @NotNull
    public static TransactionHandler getInstance() {
        return new TransactionHandlerImpl();
    }

    protected TransactionHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }
}
