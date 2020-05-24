package comp4111.handler;

import comp4111.controller.TokenManager;
import comp4111.exception.HttpHandlingException;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * This class is modified from {@link HttpEndpointHandler}. A handler which binds to a specific {@link HttpEndpoint}.
 */
public abstract class HttpAsyncEndpointHandler<ASYNC_T> implements AsyncServerRequestHandler<Message<HttpRequest, String>>, HttpEndpoint {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected CompletableFuture<ASYNC_T> handleAsync(Message<HttpRequest, String> requestObject) {
        throw new IllegalStateException("Method not implemented");
    }

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
     * @throws IllegalArgumentException if {@code requestObject} is sent using an incompatible method. If this exception
     *                                  is thrown, the response code of {@code response} will be set appropriately.
     */
    @Deprecated(forRemoval = true)
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
     * Asynchronously checks whether the method used in a request matches the accepted method by this handler.
     *
     * @param requestObject HTTP request to check.
     * @throws CompletionException if {@code requestObject} is sent using an incompatible method. Its cause is always a
     *                             {@link HttpHandlingException}.
     */
    protected final Message<HttpRequest, String> checkMethodAsync(@NotNull Message<HttpRequest, String> requestObject) {
        final Method method = HttpUtils.toMethodOrNull(requestObject.getHead().getMethod());
        if (method == null || !method.equals(getHandleMethod())) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_METHOD_NOT_ALLOWED));
        }
        return requestObject;
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
    @Deprecated(forRemoval = true)
    protected static String getToken(@NotNull Map<String, String> queryParams, @NotNull ResponseTrigger responseTrigger,
                                     @NotNull HttpContext context) throws IOException, HttpException {
        if (!queryParams.containsKey("token")) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException();
        }

        return queryParams.get("token");
    }

    @NotNull
    protected static String getTokenAsync(@NotNull Message<HttpRequest, String> requestObject) {
        final var queryParams = HttpUtils.parseQueryParamsAsync(requestObject.getHead().getPath());

        if (!queryParams.containsKey("token")) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
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
    @Deprecated(forRemoval = true)
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

    @NotNull
    protected final Message<HttpRequest, String> checkTokenAsync(@NotNull Message<HttpRequest, String> requestObject) {
        final var token = getTokenAsync(requestObject);
        if (!getTokenMgr().containsToken(token)) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
        }
        return requestObject;
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
    @Deprecated(forRemoval = true)
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

    /**
     * Retrieves the payload of the request.
     *
     * @param requestObject The HTTP request.
     * @return A string of the payload.
     * @throws IllegalArgumentException if there is no payload associated with the request. If this exception is thrown,
     *                                  the response code of {@code response} will be set appropriately.
     */
    @NotNull
    protected static String getPayloadAsync(@NotNull Message<HttpRequest, String> requestObject) {
        if (requestObject.getBody() == null || requestObject.getBody().isEmpty()) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, "Payload must be specified"));
        }
        return requestObject.getBody();
    }

    @NotNull
    protected AsyncResponseProducer exceptionToResponse(@NotNull Throwable tr) {
        final int status;
        final String reason;

        LOGGER.error("Caught exception while processing request", tr);

        if (tr.getCause() instanceof HttpHandlingException) {
            final var cause = (HttpHandlingException) tr.getCause();

            status = cause.getHttpStatus();
            reason = cause.getLocalizedMessage() != null ? cause.getLocalizedMessage() : tr.getLocalizedMessage();

            if (status == HttpStatus.SC_NOT_FOUND) {
                final var response = new BasicHttpResponse(HttpStatus.SC_NOT_FOUND, "No book record");
                if (reason != null) {
                    return new BasicResponseProducer(response, new BasicAsyncEntityProducer(reason, ContentType.TEXT_PLAIN));
                } else {
                    return new BasicResponseProducer(response);
                }
            } else if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                return AsyncResponseBuilder.create(HttpStatus.SC_METHOD_NOT_ALLOWED)
                        .setHeader(HttpHeaders.ALLOW, getHandleMethod().toString())
                        .build();
            }
        } else {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            reason = tr.getLocalizedMessage();
        }

        final var responseBuilder = AsyncResponseBuilder.create(status);
        if (reason != null && status != HttpStatus.SC_NO_CONTENT) {
            responseBuilder.setEntity(reason, ContentType.TEXT_PLAIN);
        }

        LOGGER.trace("status={} reason={}", status, reason);

        return responseBuilder.build();
    }

    protected static void emitResponse(
            @NotNull AsyncResponseProducer response,
            @NotNull ResponseTrigger responseTrigger,
            @NotNull HttpContext context) {
        final var LOGGER = LoggerFactory.getLogger(HttpAsyncEndpointHandler.class);

        try {
            LOGGER.trace("emit response {}", response);
            responseTrigger.submitResponse(response, context);
            LOGGER.trace("response done");
        } catch (Throwable ex) {
            throw new CompletionException("Error while emitting response", ex);
        }
    }
}
