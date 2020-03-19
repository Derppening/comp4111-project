package comp4111.handler.impl;

import comp4111.handler.HttpEndpointHandler;
import comp4111.handler.TransactionHandler;
import comp4111.handler.TransactionPostHandler;
import comp4111.handler.TransactionPutHandler;
import org.apache.hc.core5.http.Method;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionHandlerImpl extends TransactionHandler {

    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            TransactionPostHandler.getInstance(),
            TransactionPutHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @Override
    public Map<Method, HttpEndpointHandler> getMethodLut() {
        return METHOD_LUT;
    }
}
