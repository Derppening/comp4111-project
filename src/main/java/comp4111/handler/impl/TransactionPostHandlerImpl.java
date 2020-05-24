package comp4111.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.TransactionPostDataAccess;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.HttpAsyncEndpointHandler;
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
import java.util.concurrent.CompletionException;

public class TransactionPostHandlerImpl extends TransactionPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {

        super.handleAsync(requestObject)
                .thenApplyAsync(txRequest -> {
                    if (txRequest != null) {
                        return handleTransactionCommitRequestAsync(txRequest);
                    } else {
                        return handleTransactionIdRequestAsync();
                    }
                })
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }

    private AsyncResponseProducer handleTransactionIdRequestAsync() {
        final Long id = TransactionPostDataAccess.startNewTransaction();
        final var transactionResponse = new TransactionPostResult(id);

        if (id == 0) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
        }

        try {
            return AsyncResponseBuilder.create(HttpStatus.SC_OK)
                    .setEntity(objectMapper.writeValueAsString(transactionResponse), ContentType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transaction ID response", e);
        }
    }

    private AsyncResponseProducer handleTransactionCommitRequestAsync(@NotNull TransactionPostRequest request) {
        final boolean isSuccessful = TransactionPostDataAccess.commitOrCancelTransaction(
                request.getTransaction(),
                request.getOperation()
        );

        final AsyncResponseProducer response;
        if (isSuccessful) {
            response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
        } else {
            response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
        }
        return response;
    }
}
