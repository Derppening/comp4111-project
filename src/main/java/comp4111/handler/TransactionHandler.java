package comp4111.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.controller.TokenManager;
import comp4111.controller.TransactionManager;
import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPostResult;
import comp4111.model.TransactionPutRequest;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TransactionHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/transaction";

    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = List.of(
            new TransactionPostHandler(),
            new TransactionPutHandler()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpEndpointHandler::getHandleMethod, Function.identity()));

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @Override
            public @NotNull String getHandlePattern() {
                return HANDLE_PATTERN;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());

        HttpEndpointHandler handler = null;
        if (method != null) {
            handler = METHOD_LUT.get(method);
        }

        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", METHOD_LUT.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }
}

final class TransactionPostHandler extends HttpEndpointHandler {

    private final TransactionManager transactionMgr = TransactionManager.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenMgr = TokenManager.getInstance();

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
            return;
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (request.getEntity() == null) {
            LOGGER.info("POST /transaction token=\"{}\"", token);

            handleTransactionIdRequest(response);
        } else {
            final var payload = request.getEntity().getContent().readAllBytes();

            final TransactionPostRequest txRequest;
            try {
                txRequest = objectMapper.readValue(payload, TransactionPostRequest.class);
            } catch (Exception e) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN));
                return;
            }

            LOGGER.info("POST /transaction token=\"{}\" transaction=\"{}\" operation={}", token, txRequest.getTransaction(), txRequest.getOperation());
            handleTransactionCommitRequest(txRequest, response);
        }
    }

    private void handleTransactionIdRequest(@NotNull ClassicHttpResponse response) {
        final var uuid = transactionMgr.newTransaction();

        final var transactionResponse = new TransactionPostResult(uuid);

        try {
            response.setEntity(new StringEntity(objectMapper.writeValueAsString(transactionResponse), ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transaction ID response", e);
        }

        response.setCode(HttpStatus.SC_OK);
    }

    private void handleTransactionCommitRequest(@NotNull TransactionPostRequest request, @NotNull ClassicHttpResponse response) {
        final var result = transactionMgr.performTransaction(request);

        if (result) {
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        }
    }
}

final class TransactionPutHandler extends HttpEndpointHandler {

    private final TransactionManager transactionMgr = TransactionManager.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenManager tokenMgr = TokenManager.getInstance();

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
            return;
        }

        final var token = queryParams.get("token");
        if (!tokenMgr.containsToken(token)) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (request.getEntity() == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Payload must be specified", ContentType.TEXT_PLAIN));
            return;
        }
        final var payload = request.getEntity().getContent().readAllBytes();

        final TransactionPutRequest putRequest;
        try {
            putRequest = objectMapper.readValue(payload, TransactionPutRequest.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_PLAIN));
            return;
        }

        LOGGER.info("PUT /transaction token=\"{}\" transaction={} id={} action={}",
                token,
                putRequest.getTransaction(),
                putRequest.getId(),
                putRequest.getAction());

        final var result = transactionMgr.addTransactionPlan(putRequest);

        if (result) {
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        }
    }
}