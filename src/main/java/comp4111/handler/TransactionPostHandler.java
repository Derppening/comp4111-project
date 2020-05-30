package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.TransactionPostHandlerImpl;
import comp4111.model.TransactionPostRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class TransactionPostHandler extends HttpAsyncEndpointHandler<TransactionPostRequest> {

    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {
        @Override
        public @NotNull String getHandlePattern() {
            return TransactionHandler.HANDLE_PATTERN;
        }

        @Override
        public @NotNull Method getHandleMethod() {
            return Method.POST;
        }
    };

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Nullable
    private TransactionPostRequest txRequest;

    @NotNull
    public static TransactionPostHandler getInstance() {
        return new TransactionPostHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    protected CompletableFuture<TransactionPostRequest> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(this::checkMethodAsync)
                .thenApplyAsync(this::checkTokenAsync)
                .thenApplyAsync(request -> {
                    if (request.getBody() == null || request.getBody().isEmpty()) {
                        LOGGER.info("POST /transaction");
                        return null;
                    } else {
                        final var payload = HttpAsyncEndpointHandler.getPayloadAsync(request);
                        try {
                            txRequest = objectMapper.readValue(payload, TransactionPostRequest.class);
                        } catch (Exception e) {
                            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
                        }

                        LOGGER.info("POST /transaction transaction=\"{}\" operation={}",
                                txRequest.getTransaction(),
                                txRequest.getOperation());

                        return txRequest;
                    }
                });
    }

    @Nullable
    TransactionPostRequest getTxRequest() {
        return txRequest;
    }
}
