package comp4111.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.controller.TransactionManager;
import comp4111.dal.TransactionPostDataAccess;
import comp4111.handler.TransactionPostHandler;
import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPostResult;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TransactionPostHandlerImpl extends TransactionPostHandler {

    private final TransactionManager transactionMgr = TransactionManager.getInstance();
    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        final var txRequest = getTxRequest();
        if (txRequest == null) {
            handleTransactionIdRequest(response);
        } else {
            handleTransactionCommitRequest(txRequest, response);
        }
    }

    private void handleTransactionIdRequest(@NotNull ClassicHttpResponse response) {
        final int id = TransactionPostDataAccess.startNewTransaction();
        final var transactionResponse = new TransactionPostResult(id);

        if (id == 0) {
            response.setCode(HttpStatus.SC_TOO_MANY_REQUESTS);
            return;
        }

        try {
            response.setEntity(new StringEntity(objectMapper.writeValueAsString(transactionResponse), ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transaction ID response", e);
        }

        response.setCode(HttpStatus.SC_OK);
    }

    private void handleTransactionCommitRequest(@NotNull TransactionPostRequest request, @NotNull ClassicHttpResponse response) {
        final var transactionList = transactionMgr.getAndEraseTransaction(request);

        // TODO: Pass transactionList to DB

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}
