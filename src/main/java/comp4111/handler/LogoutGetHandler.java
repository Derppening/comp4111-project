package comp4111.handler;

import comp4111.handler.impl.LogoutGetHandlerImpl;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /logout} GET requests.
 */
public abstract class LogoutGetHandler extends HttpEndpointHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/logout";

    private String token;

    @NotNull
    public static LogoutGetHandler getInstance() {
        return new LogoutGetHandlerImpl();
    }

    @NotNull
    @Override
    public HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
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
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());
        if (method == null || !method.equals(getHandleMethod())) {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", getHandleMethod());
            throw new IllegalArgumentException();
        }

        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Token must be provided."));
            throw new IllegalArgumentException();
        }

        token = queryParams.get("token");

        LOGGER.info("GET /logout token=\"{}\"", token);
    }

    public String getToken() {
        return token;
    }
}
