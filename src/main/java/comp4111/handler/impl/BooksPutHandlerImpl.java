package comp4111.handler.impl;

import comp4111.dal.BooksPutDataAccess;
import comp4111.handler.BooksPutHandler;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksPutHandlerImpl extends BooksPutHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        int booksPutResult = BooksPutDataAccess.updateBook(null, getBookId(), getAvailable());
        if (booksPutResult == 0) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
            responseTrigger.submitResponse(response, context);
        } else if (booksPutResult == 1) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
        } else {
            // Looked through "https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/apidocs/org/apache/hc/core5/http/nio".
            // There appears to be no way to set the reason phrase to get "HTTP/1.1 404 No book record", so the following is a workaround.
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_NOT_FOUND)
                    .setHeader("Reason", "No book record").build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
