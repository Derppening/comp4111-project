package comp4111.handler;

import comp4111.handler.impl.LogoutHandlerImpl;
import org.jetbrains.annotations.NotNull;

public abstract class LogoutHandler extends HttpAsyncPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/logout";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @Override
        public @NotNull String getHandlePattern() {
            return HANDLE_PATTERN;
        }
    };

    @NotNull
    public static LogoutHandler getInstance() {
        return new LogoutHandlerImpl();
    }

    protected LogoutHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }
}
