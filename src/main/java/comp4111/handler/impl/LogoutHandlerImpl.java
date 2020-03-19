package comp4111.handler.impl;

import comp4111.handler.HttpEndpointHandler;
import comp4111.handler.LogoutGetHandler;
import comp4111.handler.LogoutHandler;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LogoutHandlerImpl extends LogoutHandler {

    private static final Map<Method, Supplier<HttpEndpointHandler>> METHOD_LUT = List.<Supplier<HttpEndpointHandler>>of(
            LogoutGetHandler::getInstance
    ).stream().collect(Collectors.toUnmodifiableMap(it -> it.get().getHandleMethod(), Function.identity()));

    @Nullable
    @Override
    public Map<Method, Supplier<HttpEndpointHandler>> getMethodLut() {
        return METHOD_LUT;
    }
}
