package comp4111.handler;

import comp4111.controller.TokenManager;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * This class is modified from {@link HttpEndpointHandler}. A handler which binds to a specific {@link HttpEndpoint}.
 */
public abstract class HttpAsyncEndpointHandler implements AsyncServerRequestHandler<Message<HttpRequest, String>>, HttpEndpoint {

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

    @Override
    public AsyncRequestConsumer<Message<HttpRequest, String>> prepare(HttpRequest request, EntityDetails entityDetails, HttpContext context) {
        return new BasicRequestConsumer<>(entityDetails != null ? new StringAsyncEntityConsumer() : null);
    }

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
     * @param requestObject HTTP request to check.
     * @param responseTrigger {@link ResponseTrigger} of the request.
     * @throws IllegalArgumentException if {@code requestObject} is sent using an incompatible method. If this exception
     *                                  is thrown, the response code of {@code response} will be set appropriately.
     */
    protected final void checkMethod(@NotNull Message<HttpRequest, String> requestObject,
                                     @NotNull ResponseTrigger responseTrigger,
                                     @NotNull HttpContext context) throws IOException, HttpException {
        final Method method = HttpUtils.toMethodOrNull(requestObject.getHead().getMethod());
        if (method == null || !method.equals(getHandleMethod())) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_METHOD_NOT_ALLOWED)
                    .setHeader(HttpHeaders.ALLOW, getHandleMethod().toString()).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Retrieves the token from the query parameters.
     *
     * @param queryParams Query parameters for this request.
     * @param responseTrigger {@link ResponseTrigger} for this request.
     * @return String representing the token.
     * @throws IllegalArgumentException if the query parameters do not contain the token. If this exception is throw,
     *                                  the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected static String getToken(@NotNull Map<String, String> queryParams, @NotNull ResponseTrigger responseTrigger,
                                     @NotNull HttpContext context) throws IOException, HttpException {
        if (!queryParams.containsKey("token")) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        return queryParams.get("token");
    }

    /**
     * Retrieves the token from the query parameters and checks whether it is correct.
     *
     * @param queryParams Query parameters for this request.
     * @param responseTrigger {@link ResponseTrigger} for this request.
     * @return String representing the token.
     * @throws IllegalArgumentException if the query parameters do not contain the token, or the token is incorrect. If
     *                                  this exception is throw, the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected final String checkToken(@NotNull Map<String, String> queryParams,
                                      @NotNull ResponseTrigger responseTrigger,
                                      @NotNull HttpContext context) throws IOException, HttpException {
        final var token = getToken(queryParams, responseTrigger, context);
        if (!getTokenMgr().containsToken(token)) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        return token;
    }

    /**
     * Retrieves the payload of the request.
     *
     * @param requestObject The HTTP request.
     * @param responseTrigger {@link ResponseTrigger} of the request.
     * @return A string of the payload.
     * @throws IllegalArgumentException if there is no payload associated with the request. If this exception is thrown,
     *                                  the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected static String getPayload(@NotNull Message<HttpRequest, String> requestObject,
                                       @NotNull ResponseTrigger responseTrigger, @NotNull HttpContext context)
            throws IOException, HttpException {
        if (requestObject.getBody() == null || requestObject.getBody().isEmpty()) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST)
                    .setEntity("Payload must be specified", ContentType.TEXT_PLAIN).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }
        return requestObject.getBody();
    }
}
