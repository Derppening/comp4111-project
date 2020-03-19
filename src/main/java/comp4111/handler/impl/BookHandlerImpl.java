package comp4111.handler.impl;

import comp4111.handler.BookDeleteHandler;
import comp4111.handler.BookHandler;
import comp4111.handler.BookPutHandler;
import comp4111.handler.HttpEndpointHandler;
import org.apache.hc.core5.http.Method;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookHandlerImpl extends BookHandler {

    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            BookPutHandler.getInstance(),
            BookDeleteHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @Override
    public Map<Method, HttpEndpointHandler> getMethodLut() {
        return METHOD_LUT;
    }
}