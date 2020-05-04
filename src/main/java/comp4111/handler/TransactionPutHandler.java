package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.TransactionPutHandlerImpl;
import comp4111.model.TransactionPutRequest;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public abstract class TransactionPutHandler extends HttpAsyncEndpointHandler {

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
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final var queryParams = HttpUtils.parseQueryParams(requestObject.getHead().getPath(), responseTrigger, context);
        final var token = checkToken(queryParams, responseTrigger, context);

        final var payload = getPayload(requestObject, responseTrigger, context);

        try {
            putRequest = objectMapper.readValue(payload, TransactionPutRequest.class);
        } catch (Exception e) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST)
                    .setEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("PUT /transaction token=\"{}\" transaction={} id={} action={}",
                token,
                putRequest.getTransaction(),
                putRequest.getId(),
                putRequest.getAction());
    }

    @NotNull
    protected TransactionPutRequest getPutRequest() {
        return Objects.requireNonNull(putRequest);
    }
}
