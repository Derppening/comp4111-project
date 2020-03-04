package comp4111.handler;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /logout} GET requests.
 */
public final class LogoutGetHandler extends HttpEndpointHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/logout";

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
            return;
        }

        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Token must be provided."));
            return;
        }

        final var token = queryParams.get("token");

        LOGGER.info("GET /logout token=\"{}\"", token);

        // TODO(Derppening): Handle logout request

        response.setCode(HttpStatus.SC_OK);
    }
}
