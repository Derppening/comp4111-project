package comp4111.handler;

import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;

/**
 * An endpoint on the HTTP server.
 */
public interface HttpEndpoint extends HttpPath {

    /**
     * @return The method this endpoint supports.
     */
    @NotNull
    Method getHandleMethod();
}
