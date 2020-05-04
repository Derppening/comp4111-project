package comp4111.handler.impl;

import comp4111.dal.BooksPostDataAccess;
import comp4111.handler.BooksPostHandler;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksPostHandlerImpl extends BooksPostHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        long bookId = BooksPostDataAccess.getBook(getBook().getTitle());
        if (bookId == 0) {
            // The book does not exist.
            long newBookId = BooksPostDataAccess.addBook(getBook().getTitle(), getBook().getAuthor(), getBook().getPublisher(), getBook().getYear());

            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_CREATED)
                    .setHeader(HttpHeaders.LOCATION, "/books/" + newBookId)
                    .setEntity(String.format("%s%s/%d?token=%s",
                            HttpUtils.getServerHostnameFromRequest(requestObject),
                            getHandlePattern(),
                            newBookId,
                            getToken()), ContentType.TEXT_PLAIN).build();
            responseTrigger.submitResponse(response, context);
        } else {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_CONFLICT)
                    .setHeader("Duplicate record", "/books/" + bookId).build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
