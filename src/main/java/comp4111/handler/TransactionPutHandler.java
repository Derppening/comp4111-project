package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.TransactionPutHandlerImpl;
import comp4111.model.TransactionPutRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class TransactionPutHandler extends HttpAsyncEndpointHandler<TransactionPutRequest> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @Override
        public @NotNull String getHandlePattern() {
            return TransactionHandler.HANDLE_PATTERN;
        }

        @Override
        public @NotNull Method getHandleMethod() {
            return Method.PUT;
        }
    };

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Nullable
    private TransactionPutRequest putRequest;

    @NotNull
    public static TransactionPutHandler getInstance() {
        return new TransactionPutHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    protected CompletableFuture<TransactionPutRequest> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(this::checkTokenAsync)
                .thenApplyAsync(HttpAsyncEndpointHandler::getPayloadAsync)
                .thenApplyAsync(payload -> {
                    try {
                        putRequest = objectMapper.readValue(payload, TransactionPutRequest.class);
                    } catch (Exception e) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
                    }
                    return putRequest;
                })
                .thenApplyAsync(putRequest -> {
                    LOGGER.info("PUT /transaction transaction={} id={} action={}",
                            putRequest.getTransaction(),
                            putRequest.getId(),
                            putRequest.getAction());
                    return putRequest;
                });
    }

    @NotNull
    TransactionPutRequest getPutRequest() {
        return Objects.requireNonNull(putRequest);
    }
}
