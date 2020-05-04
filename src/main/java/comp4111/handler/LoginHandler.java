package comp4111.handler;

import comp4111.handler.impl.LoginHandlerImpl;
import org.jetbrains.annotations.NotNull;

public abstract class LoginHandler extends HttpAsyncPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/login";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {

        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN;
        }
    };

    @NotNull
    public static LoginHandler getInstance() {
        return new LoginHandlerImpl();
    }

    protected LoginHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }
}
