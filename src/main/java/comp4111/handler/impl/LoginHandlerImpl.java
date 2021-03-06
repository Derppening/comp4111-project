package comp4111.handler.impl;

import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.LoginHandler;
import comp4111.handler.LoginPostHandler;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LoginHandlerImpl extends LoginHandler {

    private static final Map<Method, Supplier<HttpAsyncEndpointHandler<?>>> METHOD_LUT = List.<Supplier<HttpAsyncEndpointHandler<?>>>of(
            LoginPostHandler::getInstance
    ).stream().collect(Collectors.toUnmodifiableMap(it -> it.get().getHandleMethod(), Function.identity()));

    @Nullable
    @Override
    public Map<Method, Supplier<HttpAsyncEndpointHandler<?>>> getMethodLut() {
        return METHOD_LUT;
    }
}
