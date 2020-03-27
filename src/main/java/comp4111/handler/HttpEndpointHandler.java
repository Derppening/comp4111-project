package comp4111.handler;

import comp4111.controller.TokenManager;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * A handler which binds to a specific {@link HttpEndpoint}.
 */
public abstract class HttpEndpointHandler implements HttpRequestHandler, HttpEndpoint {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @NotNull
    protected TokenManager getTokenMgr() {
        return TokenManager.getInstance();
    }

    /**
     * @return The handler definition, which may be any object which inherits from {@link HttpEndpoint}.
     */
    @NotNull
    public abstract HttpEndpoint getHandlerDefinition();

    /**
     * @return The path pattern that this class handles.
     */
    @Override
    public final @NotNull String getHandlePattern() {
        return getHandlerDefinition().getHandlePattern();
    }

    /**
     * @return The HTTP method that this class handles.
     */
    @NotNull
    @Override
    public final Method getHandleMethod() {
        return getHandlerDefinition().getHandleMethod();
    }

    /**
     * Checks whether the method used in a request matches the accepted method by this handler.
     *
     * @param request HTTP request to check.
     * @param response HTTP response for this request.
     * @throws IllegalArgumentException if {@code request} is sent using an incompatible method. If this exception is
     *                                  thrown, the response code of {@code response} will be set appropriately
     */
    protected final void checkMethod(@NotNull ClassicHttpRequest request, @NotNull ClassicHttpResponse response) {
        final Method method = HttpUtils.toMethodOrNull(request.getMethod());
        if (method == null || !method.equals(getHandleMethod())) {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", getHandleMethod());
            throw new IllegalArgumentException();
        }
    }

    /**
     * Retrieves the token from the query parameters.
     *
     * @param queryParams Query parameters for this request.
     * @param response HTTP response for this request.
     * @return String representing the token.
     * @throws IllegalArgumentException if the query parameters do not contain the token. If this exception is throw,
     *                                  the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected static String getToken(@NotNull Map<String, String> queryParams, @NotNull ClassicHttpResponse response) {
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            throw new IllegalArgumentException();
        }

        return queryParams.get("token");
    }

    /**
     * Retrieves the token from the query parameters and checks whether it is correct.
     *
     * @param queryParams Query parameters for this request.
     * @param response HTTP response for this request.
     * @return String representing the token.
     * @throws IllegalArgumentException if the query parameters do not contain the token, or the token is incorrect. If
     *                                  this exception is throw, the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected final String checkToken(@NotNull Map<String, String> queryParams, @NotNull ClassicHttpResponse response) {
        final var token = getToken(queryParams, response);
        if (!getTokenMgr().containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            throw new IllegalArgumentException();
        }

        return token;
    }

    /**
     * Retrieves the payload of the request.
     *
     * @param request The HTTP request.
     * @param response The HTTP response to the request.
     * @return A byte array of the payload.
     * @throws IllegalArgumentException if there is no payload associated with the request. If this exception is thrown,
     *                                  the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected static byte[] getPayload(@NotNull ClassicHttpRequest request, @NotNull ClassicHttpResponse response) throws IOException {
        if (request.getEntity() == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Payload must be specified", ContentType.TEXT_PLAIN));
            throw new IllegalArgumentException();
        }
        return request.getEntity().getContent().readAllBytes();
    }
}
