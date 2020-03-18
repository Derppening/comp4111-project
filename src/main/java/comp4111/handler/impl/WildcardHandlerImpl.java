package comp4111.handler.impl;

import comp4111.handler.WildcardHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

public class WildcardHandlerImpl extends WildcardHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        response.setCode(HttpStatus.SC_NOT_FOUND);
    }
}
