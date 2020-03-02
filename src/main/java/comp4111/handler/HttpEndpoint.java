package comp4111.handler;

import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;

public interface HttpEndpoint extends HttpPath {

    @NotNull
    Method getHandleMethod();
}
