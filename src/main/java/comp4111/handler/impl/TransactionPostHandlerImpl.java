package comp4111.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.TransactionPostDataAccess;
import comp4111.handler.TransactionPostHandler;
import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPostResult;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class TransactionPostHandlerImpl extends TransactionPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        final var txRequest = getTxRequest();
        if (txRequest == null) {
            handleTransactionIdRequest(responseTrigger, context);
        } else {
            handleTransactionCommitRequest(txRequest, responseTrigger, context);
        }
    }

    private void handleTransactionIdRequest(@NotNull ResponseTrigger responseTrigger, @NotNull HttpContext context)
            throws IOException, HttpException {
        final Long id = TransactionPostDataAccess.startNewTransaction();
        final var transactionResponse = new TransactionPostResult(id);

        if (id == 0) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            return;
        }

        try {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_OK)
                    .setEntity(objectMapper.writeValueAsString(transactionResponse), ContentType.APPLICATION_JSON).build();
            responseTrigger.submitResponse(response, context);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transaction ID response", e);
        }
    }

    private void handleTransactionCommitRequest(@NotNull TransactionPostRequest request,
                                                @NotNull ResponseTrigger responseTrigger,
                                                @NotNull HttpContext context) throws IOException, HttpException {
        final boolean isSuccessful = TransactionPostDataAccess.commitOrCancelTransaction(
                Objects.requireNonNull(getTxRequest()).getTransaction(),
                getTxRequest().getOperation()
        );

        final AsyncResponseProducer response;
        if (isSuccessful) {
            response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
        } else {
            response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
        }
        responseTrigger.submitResponse(response, context);
    }
}
