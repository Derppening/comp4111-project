package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.TransactionPutHandlerImpl;
import comp4111.model.TransactionPutRequest;
import comp4111.util.HttpUtils;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class TransactionPutHandler extends HttpEndpointHandler {

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
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = HttpUtils.parseQueryParams(request.getPath());
        final var token = checkToken(queryParams, response);

        final var payload = getPayload(request, response);

        try {
            putRequest = objectMapper.readValue(payload, TransactionPutRequest.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN));
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("PUT /transaction token=\"{}\" transaction={} id={} action={}",
                token,
                putRequest.getTransaction(),
                putRequest.getId(),
                putRequest.getAction());
    }

    protected TransactionPutRequest getPutRequest() {
        return putRequest;
    }
}
