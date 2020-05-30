package comp4111.handler.impl;

import comp4111.dal.TransactionPutDataAccess;
import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.TransactionPutHandler;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

public class TransactionPutHandlerImpl extends TransactionPutHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {

        super.handleAsync(requestObject)
                .thenApplyAsync(request -> TransactionPutDataAccess.pushAction(request.getTransaction(), request.getId(), request.getAction()))
                .thenApplyAsync(result -> {
                    if (result == 0) {
                        return AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
                    } else {
                        return AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
                    }
                })
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }
}
