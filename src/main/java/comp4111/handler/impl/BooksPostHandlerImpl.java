package comp4111.handler.impl;

import comp4111.dal.BooksPostDataAccess;
import comp4111.handler.BooksPostHandler;
import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

public class BooksPostHandlerImpl extends BooksPostHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {
        super.handleAsync(requestObject)
                .thenApplyAsync(request -> {
                    final var bookId = BooksPostDataAccess.getBook(request.book.getTitle());
                    if (bookId == 0) {
                        long newBookId = BooksPostDataAccess.addBook(request.book);

                        return AsyncResponseBuilder.create(HttpStatus.SC_CREATED)
                                .setHeader(HttpHeaders.LOCATION, "/books/" + newBookId)
                                .setEntity(String.format("%s%s/%d?token=%s",
                                        HttpUtils.getServerHostnameFromRequest(requestObject),
                                        getHandlePattern(),
                                        newBookId,
                                        request.token), ContentType.TEXT_PLAIN).build();
                    } else {
                        return AsyncResponseBuilder.create(HttpStatus.SC_CONFLICT)
                                .setHeader("Duplicate record", "/books/" + bookId)
                                .build();
                    }
                })
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }
}
