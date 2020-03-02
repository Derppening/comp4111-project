package comp4111.handler;

import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.jetbrains.annotations.NotNull;

/**
 * An extension for {@link HttpRequestHandler} which also allows a class to specify the pattern it handles.
 */
public abstract class HttpEndpointHandler extends HttpPathHandler implements HttpEndpoint {

    @NotNull
    public abstract HttpEndpoint getHandlerDefinition();

    @NotNull
    @Override
    public final Method getHandleMethod() {
        return getHandlerDefinition().getHandleMethod();
    }
}
