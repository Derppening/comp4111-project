package comp4111.handler;

import comp4111.handler.impl.LogoutGetHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /logout} GET requests.
 */
public abstract class LogoutGetHandler extends HttpEndpointHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/logout";
    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @NotNull
        @Override
        public Method getHandleMethod() {
            return Method.GET;
        }

        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN;
        }
    };

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
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        checkMethod(request, response);

        final var queryParams = parseQueryParams(request.getPath());

        token = getToken(queryParams, response);

        LOGGER.info("GET /logout token=\"{}\"", token);
    }

    public String getToken() {
        return token;
    }
}
