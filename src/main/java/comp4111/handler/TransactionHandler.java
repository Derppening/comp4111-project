package comp4111.handler;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TransactionHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/transaction";

    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            TransactionPostHandler.getInstance(),
            TransactionPutHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @NotNull
    public static TransactionHandler getInstance() {
        return new TransactionHandler();
    }

    private TransactionHandler() {
    }

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @Override
            public @NotNull String getHandlePattern() {
                return HANDLE_PATTERN;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());

        HttpEndpointHandler handler = null;
        if (method != null) {
            handler = METHOD_LUT.get(method);
        }

        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", METHOD_LUT.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }
}

