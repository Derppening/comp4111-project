package comp4111.handler;

import comp4111.handler.impl.LogoutGetHandlerImpl;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Endpoint handler for all {@code /logout} GET requests.
 */
public abstract class LogoutGetHandler extends HttpAsyncEndpointHandler<String> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @NotNull
        @Override
        public Method getHandleMethod() {
            return Method.GET;
        }

        @NotNull
        @Override
        public String getHandlePattern() {
            return LogoutHandler.HANDLE_PATTERN;
        }
    };

    @Nullable
    private String token;

    @NotNull
    public static LogoutGetHandler getInstance() {
        return new LogoutGetHandlerImpl();
    }

    @NotNull
    @Override
    public HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    protected CompletableFuture<String> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(request -> {
                    token = HttpAsyncEndpointHandler.getTokenAsync(request);
                    return token;
                })
                .thenApplyAsync(token -> {
                    LOGGER.info("GET /logout token=\"{}\"", token);
                    return token;
                });
    }

    @NotNull
    String getToken() {
        return Objects.requireNonNull(token);
    }
}
