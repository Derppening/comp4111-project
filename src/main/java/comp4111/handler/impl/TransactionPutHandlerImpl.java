package comp4111.handler.impl;

import comp4111.controller.TransactionManager;
import comp4111.handler.TransactionPutHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class TransactionPutHandlerImpl extends TransactionPutHandler {

    private final TransactionManager transactionMgr = TransactionManager.getInstance();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        final var result = transactionMgr.addTransactionPlan(getPutRequest());

        if (result) {
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        }
    }
}
