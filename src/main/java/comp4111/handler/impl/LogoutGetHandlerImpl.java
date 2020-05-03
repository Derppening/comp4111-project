package comp4111.handler.impl;

import comp4111.handler.LogoutGetHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class LogoutGetHandlerImpl extends LogoutGetHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (getTokenMgr().removeToken(getToken())) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
            responseTrigger.submitResponse(response, context);
        } else {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
