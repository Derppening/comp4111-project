package comp4111.handler.impl;

import comp4111.handler.LogoutGetHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class LogoutGetHandlerImpl extends LogoutGetHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (getTokenMgr().removeToken(getToken())) {
            response.setCode(HttpStatus.SC_OK);
        } else {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        }
    }
}
