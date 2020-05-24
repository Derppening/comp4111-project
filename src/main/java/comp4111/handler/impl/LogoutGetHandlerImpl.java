package comp4111.handler.impl;

import comp4111.exception.HttpHandlingException;
import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.LogoutGetHandler;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.CompletionException;

public class LogoutGetHandlerImpl extends LogoutGetHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {

        super.handleAsync(requestObject)
                .thenApplyAsync(token -> {
                    if (!getTokenMgr().removeToken(token)) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
                    }
                    return AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
                })
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }
}
