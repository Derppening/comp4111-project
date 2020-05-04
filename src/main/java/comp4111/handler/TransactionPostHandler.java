package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.TransactionPostHandlerImpl;
import comp4111.model.TransactionPostRequest;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class TransactionPostHandler extends HttpAsyncEndpointHandler {

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
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final var queryParams = HttpUtils.parseQueryParams(requestObject.getHead().getPath(), responseTrigger, context);
        final var token = checkToken(queryParams, responseTrigger, context);

        if (requestObject.getBody() == null || requestObject.getBody().isEmpty()) {
            LOGGER.info("POST /transaction token=\"{}\"", token);
        } else {
            final String payload = requestObject.getBody();

            try {
                txRequest = objectMapper.readValue(payload, TransactionPostRequest.class);
            } catch (Exception e) {
                final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST)
                        .setEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN).build();
                responseTrigger.submitResponse(response, context);
                throw new IllegalArgumentException(e);
            }

            LOGGER.info("POST /transaction token=\"{}\" transaction=\"{}\" operation={}", token, txRequest.getTransaction(), txRequest.getOperation());
        }
    }

    @Nullable
    protected TransactionPostRequest getTxRequest() {
        return txRequest;
    }
}
