package comp4111.handler.impl;

import comp4111.handler.BooksGetHandler;
import comp4111.handler.BooksHandler;
import comp4111.handler.BooksPostHandler;
import comp4111.handler.HttpEndpointHandler;
import org.apache.hc.core5.http.Method;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BooksHandlerImpl extends BooksHandler {

    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            BooksGetHandler.getInstance(),
            BooksPostHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @Override
    public Map<Method, HttpEndpointHandler> getMethodLut() {
        return METHOD_LUT;
    }
}
