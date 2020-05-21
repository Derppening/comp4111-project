package comp4111.handler.impl;

import comp4111.dal.BooksDeleteDataAccess;
import comp4111.handler.BooksDeleteHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksDeleteHandlerImpl extends BooksDeleteHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        boolean isSuccessful = BooksDeleteDataAccess.deleteBook(getBookId());
        final AsyncResponseProducer response;
        if (isSuccessful) {
            response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
        } else {
            response = new BasicResponseProducer(new BasicHttpResponse(HttpStatus.SC_NOT_FOUND, "No book record"));
        }
        responseTrigger.submitResponse(response, context);
    }
}
