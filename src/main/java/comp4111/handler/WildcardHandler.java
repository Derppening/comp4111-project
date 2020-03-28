package comp4111.handler;

import comp4111.handler.impl.WildcardHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

/**
 * Path handler for all {@code *} requests.
 */
public abstract class WildcardHandler extends HttpPathHandler {

    @NotNull
    public static WildcardHandler getInstance() {
        return new WildcardHandlerImpl();
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @Override
            public @NotNull String getHandlePattern() {
                return "*";
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
    }
}
