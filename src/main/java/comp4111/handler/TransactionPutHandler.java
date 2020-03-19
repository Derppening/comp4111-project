package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.TransactionPutHandlerImpl;
import comp4111.model.TransactionPutRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class TransactionPutHandler extends HttpEndpointHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private TransactionPutRequest putRequest;

    @NotNull
    public static TransactionPutHandler getInstance() {
        return new TransactionPutHandlerImpl();
    }

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @Override
            public @NotNull String getHandlePattern() {
                return TransactionHandler.HANDLE_PATTERN;
            }

            @Override
            public @NotNull Method getHandleMethod() {
                return Method.PUT;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            throw new IllegalArgumentException();
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            throw new IllegalArgumentException();
        }

        if (request.getEntity() == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Payload must be specified", ContentType.TEXT_PLAIN));
            throw new IllegalArgumentException();
        }
        final var payload = request.getEntity().getContent().readAllBytes();

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
