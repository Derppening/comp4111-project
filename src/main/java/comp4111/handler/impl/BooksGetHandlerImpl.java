package comp4111.handler.impl;

import comp4111.handler.BooksGetHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksGetHandlerImpl extends BooksGetHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        // TODO(Derppening): Handle QUERY operation on db

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}