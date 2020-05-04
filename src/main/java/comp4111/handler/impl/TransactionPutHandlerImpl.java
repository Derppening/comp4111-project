package comp4111.handler.impl;

import comp4111.dal.TransactionPutDataAccess;
import comp4111.handler.TransactionPutHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class TransactionPutHandlerImpl extends TransactionPutHandler {

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        final int transactionPutResult = TransactionPutDataAccess.pushAction(
                getPutRequest().getTransaction(),
                getPutRequest().getId(),
                getPutRequest().getAction()
        );

        final AsyncResponseProducer response;
        if (transactionPutResult == 0) {
            response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
        } else {
            response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
        }
        responseTrigger.submitResponse(response, context);
    }
}
