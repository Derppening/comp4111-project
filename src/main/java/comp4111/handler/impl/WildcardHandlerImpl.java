package comp4111.handler.impl;

import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.WildcardHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class WildcardHandlerImpl extends WildcardHandler {

    @Override
    @Nullable
    protected Map<Method, Supplier<HttpAsyncEndpointHandler>> getMethodLut() {
        return null;
    }

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        final AsyncResponseProducer response;

        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        response = AsyncResponseBuilder.create(HttpStatus.SC_NOT_FOUND).build();
        responseTrigger.submitResponse(response, context);
    }
}
