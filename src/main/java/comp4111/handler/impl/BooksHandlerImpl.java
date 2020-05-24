package comp4111.handler.impl;

import comp4111.handler.*;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BooksHandlerImpl extends BooksHandler {

    private static final Map<Method, Supplier<HttpAsyncEndpointHandler<?>>> METHOD_LUT = List.<Supplier<HttpAsyncEndpointHandler<?>>>of(
            BooksDeleteHandler::getInstance,
            BooksGetHandler::getInstance,
            BooksPostHandler::getInstance,
            BooksPutHandler::getInstance
    ).stream().collect(Collectors.toUnmodifiableMap(it -> it.get().getHandleMethod(), Function.identity()));

    @Override
    public @Nullable Map<Method, Supplier<HttpAsyncEndpointHandler<?>>> getMethodLut() {
        return METHOD_LUT;
    }
}
