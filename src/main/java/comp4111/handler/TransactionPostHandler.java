package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.controller.TokenManager;
import comp4111.handler.impl.TransactionPostHandlerImpl;
import comp4111.model.TransactionPostRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class TransactionPostHandler extends HttpEndpointHandler {

    private final TokenManager tokenMgr = TokenManager.getInstance();
    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private TransactionPostRequest txRequest;

    @NotNull
    public static TransactionPostHandler getInstance() {
        return new TransactionPostHandlerImpl();
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
                return Method.POST;
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
            LOGGER.info("POST /transaction token=\"{}\"", token);
        } else {
            final var payload = request.getEntity().getContent().readAllBytes();

            try {
                txRequest = objectMapper.readValue(payload, TransactionPostRequest.class);
            } catch (Exception e) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN));
                throw new IllegalArgumentException(e);
            }

            LOGGER.info("POST /transaction token=\"{}\" transaction=\"{}\" operation={}", token, txRequest.getTransaction(), txRequest.getOperation());
        }
    }

    protected TransactionPostRequest getTxRequest() {
        return txRequest;
    }
}
