package comp4111.handler;

import comp4111.handler.impl.TransactionHandlerImpl;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class TransactionHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/transaction";
    private static final HttpPath HANDLER_DEFINITION = new HttpPath() {
        @Override
        public @NotNull String getHandlePattern() {
            return HANDLE_PATTERN;
        }
    };

    @NotNull
    public static TransactionHandler getInstance() {
        return new TransactionHandlerImpl();
    }

    protected TransactionHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        dispatchByMethod(request, response, context, getMethodLut());
    }
}
