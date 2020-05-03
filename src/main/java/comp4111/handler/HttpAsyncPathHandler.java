package comp4111.handler;

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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is modified from {@link HttpPathHandler}. A handler which binds to a specific {@link HttpPath}.
 *
 * Since this class does not have an associated {@link Method} bound to the handler, this class should be used as either
 * a generic path handler for all methods, or a dispatcher to dispatch the request to a
 * {@link HttpAsyncEndpointHandler}.
 */
public abstract class HttpAsyncPathHandler implements AsyncServerRequestHandler<Message<HttpRequest, String>>, HttpPath {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * @return The lookup table for matching a request sent with {@link Method} to its corresponding
     * {@link HttpAsyncEndpointHandler}. Can be {@code null} to indicate that a lookup table is not applicable to this
     * handler, for example if all requests should be handled the same regardless of its method.
     * @implSpec This method should return the same value every invocation.
     */
    @Nullable
    protected abstract Map<Method, Supplier<HttpAsyncEndpointHandler>> getMethodLut();

    /**
     * @return The handler definition, which may be any object which inherits from {@link HttpPath}.
     * @implSpec This method should return the same value every invocation.
     */
    @NotNull
    public abstract HttpPath getHandlerDefinition();

    @Override
    public AsyncRequestConsumer<Message<HttpRequest, String>> prepare(HttpRequest request, EntityDetails entityDetails, HttpContext context) {
        return new BasicRequestConsumer<>(entityDetails != null ? new StringAsyncEntityConsumer() : null);
    }

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(requestObject, responseTrigger, context, Objects.requireNonNull(getMethodLut()));
    }

    /**
     * @return The path pattern that this class handles.
     * @implSpec This method should return the same value every invocation.
     */
    @NotNull
    @Override
    public final String getHandlePattern() {
        return getHandlerDefinition().getHandlePattern();
    }

    /**
     * Dispatches a request by its method.
     *
     * @param requestObject {@link Message} to dispatch. Usually the first argument of {@link AsyncServerRequestHandler#handle}.
     * @param responseTrigger {@link ResponseTrigger} of the request. Usually the second argument of {@link AsyncServerRequestHandler#handle}.
     * @param context {@link HttpContext} of the request. Usually the third argument of {@link AsyncServerRequestHandler#handle}.
     * @param lut Lookup table for matching a {@link Method} to its corresponding {@link HttpAsyncEndpointHandler} creator.
     */
    private static void dispatchByMethod(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context, Map<Method, Supplier<HttpAsyncEndpointHandler>> lut) throws HttpException, IOException {
        final AsyncResponseProducer response;
        final Method method = HttpUtils.toMethodOrNull(requestObject.getHead().getMethod());
        Supplier<HttpAsyncEndpointHandler> handler = null;

        if (method != null) {
            handler = lut.get(method);
        }

        if (handler != null) {
            handler.get().handle(requestObject, responseTrigger, context);
        } else {
            response = AsyncResponseBuilder.create(HttpStatus.SC_METHOD_NOT_ALLOWED)
                    .setHeader(HttpHeaders.ALLOW, lut.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")))
                    .build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
