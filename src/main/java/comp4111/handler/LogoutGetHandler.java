package comp4111.handler;

import comp4111.handler.impl.LogoutGetHandlerImpl;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * Endpoint handler for all {@code /logout} GET requests.
 */
public abstract class LogoutGetHandler extends HttpAsyncEndpointHandler {

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

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final var queryParams = HttpUtils.parseQueryParams(requestObject.getHead().getPath(), responseTrigger, context);

        token = getToken(queryParams, responseTrigger, context);

        LOGGER.info("GET /logout token=\"{}\"", token);
    }

    @NotNull
    public String getToken() {
        return Objects.requireNonNull(token);
    }
}
