package comp4111.handler.impl;

import comp4111.dal.BooksDeleteDataAccess;
import comp4111.handler.BooksDeleteHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
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
            // Looked through "https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/apidocs/org/apache/hc/core5/http/nio".
            // There appears to be no way to set the reason phrase to get "HTTP/1.1 404 No book record", so the following is a workaround.
            response = AsyncResponseBuilder.create(HttpStatus.SC_NOT_FOUND)
                    .setHeader("Reason", "No book record").build();
        }
        responseTrigger.submitResponse(response, context);
    }
}
