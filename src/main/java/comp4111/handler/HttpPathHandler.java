package comp4111.handler;

import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpRequestHandler;
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
 * A handler which binds to a specific {@link HttpPath}.
 *
 * Since this class does not have an associated {@link Method} bound to the handler, this class should be used as either
 * a generic path handler for all methods, or a dispatcher to dispatch the request to a {@link HttpEndpointHandler}.
 */
public abstract class HttpPathHandler implements HttpRequestHandler, HttpPath {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * @return The lookup table for matching a request sent with {@link Method} to its corresponding
     * {@link HttpEndpointHandler}. Can be {@code null} to indicate that a lookup table is not applicable to this
     * handler, for example if all requests should be handled the same regardless of its method.
     * @implSpec This method should return the same value every invocation.
     */
    @Nullable
    protected abstract Map<Method, Supplier<HttpEndpointHandler>> getMethodLut();

    /**
     * @return The handler definition, which may be any object which inherits from {@link HttpPath}.
     * @implSpec This method should return the same value evey invocation.
     */
    @NotNull
    public abstract HttpPath getHandlerDefinition();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(request, response, context, Objects.requireNonNull(getMethodLut()));
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
     * @param request {@link ClassicHttpRequest} to dispatch. Usually the first argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param response {@link ClassicHttpResponse} of the request. Usually the second argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param context {@link HttpContext} of the request. Usually the third argument of {@link HttpRequestHandler#handle(ClassicHttpRequest, ClassicHttpResponse, HttpContext)}.
     * @param lut Lookup table for matching a {@link Method} to its corresponding {@link HttpEndpointHandler} creator.
     */
    private static void dispatchByMethod(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context, Map<Method, Supplier<HttpEndpointHandler>> lut) throws HttpException, IOException {
        final Method method = HttpUtils.toMethodOrNull(request.getMethod());

        Supplier<HttpEndpointHandler> handler = null;
        if (method != null) {
            handler = lut.get(method);
        }

        if (handler != null) {
            handler.get().handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", lut.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }
}
