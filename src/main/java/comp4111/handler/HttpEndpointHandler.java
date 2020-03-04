package comp4111.handler;

import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;

/**
 * A handler which binds to a specific {@link HttpEndpoint}.
 */
public abstract class HttpEndpointHandler extends HttpPathHandler implements HttpEndpoint {

    /**
     * @return The handler definition, which may be any object which inherits from {@link HttpEndpoint}.
     */
    @NotNull
    public abstract HttpEndpoint getHandlerDefinition();

    /**
     * @return The HTTP method that this class handles.
     */
    @NotNull
    @Override
    public final Method getHandleMethod() {
        return getHandlerDefinition().getHandleMethod();
    }
}
