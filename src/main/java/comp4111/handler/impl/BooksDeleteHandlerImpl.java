package comp4111.handler.impl;

import comp4111.dal.BooksDeleteDataAccess;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.BooksDeleteHandler;
import comp4111.handler.HttpAsyncEndpointHandler;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.concurrent.CompletionException;

public class BooksDeleteHandlerImpl extends BooksDeleteHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {

        super.handleAsync(requestObject)
                .thenApplyAsync(BooksDeleteDataAccess::deleteBook)
                .thenApplyAsync(isSuccessful -> {
                    if (!isSuccessful) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_NOT_FOUND));
                    }
                    return AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
                })
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }
}
