package comp4111.handler;

import comp4111.handler.impl.WildcardHandlerImpl;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Path handler for all {@code *} requests.
 */
public abstract class WildcardHandler extends HttpAsyncPathHandler {

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
    public void handle(Message<HttpRequest, Void> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
    }
}
