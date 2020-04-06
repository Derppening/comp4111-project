package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import comp4111.controller.TransactionManager;
import comp4111.model.TransactionPostRequest;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TransactionPostHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private TransactionManager txMgr;
    private TransactionPostHandler handler;
    private ObjectMapper objectMapper;
    private String token;
    private Long transactionId;

    @BeforeEach
    public void setUp() {
        super.setUp();

        handler = new TransactionPostHandler() {
            @Override
            protected @NotNull TokenManager getTokenMgr() {
                return tokenMgr;
            }

            @Override
            public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
                try {
                    super.handle(request, response, context);
                } catch (IllegalArgumentException e) {
                    return;
                }

                response.setCode(HttpStatus.SC_OK);
            }
        };
        tokenMgr = TokenManager.getInstance();
        token = tokenMgr.newToken("user001");
        txMgr = TransactionManager.getInstance();
        transactionId = txMgr.newTransaction();

        objectMapper = new ObjectMapper();

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, handler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
            assertEquals(handler.getHandleMethod().toString(), response.getHeader("Allow").getValue());
        }
    }

    @Test
    void givenNoToken_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenBadToken_checkBadRequest() throws Exception {
        final var badToken = tokenMgr.newToken("user002");
        assumeTrue(badToken != null);
        assumeTrue(tokenMgr.removeToken(badToken));

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNoPayloadRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getTxRequest());
        }
    }

    @Test
    void givenZeroLengthPayloadRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getTxRequest());
        }
    }

    @Test
    void givenEmptyPayloadRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingTransactionRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Operation\": \"commit\"" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingOperationRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + transactionId + "\"" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullTransactionRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": null, " +
                "\"Operation\": \"commit\"" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullOperationRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": " + transactionId + ", " +
                "\"Operation\": null" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Disabled("Bad transaction UUIDs are currently checked in the overriding class.")
    @Test
    void givenBadTransactionRequest_checkOK() throws Exception {
        final var badTx = txMgr.newTransaction();
        txMgr.getAndEraseTransaction(new TransactionPostRequest(badTx, TransactionPostRequest.Operation.CANCEL));

        final var postRequest = new TransactionPostRequest(badTx, TransactionPostRequest.Operation.COMMIT);
        final var payload = objectMapper.writeValueAsString(postRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenBadOperationRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": " + transactionId + ", " +
                "\"Operation\": \"dance\"" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenGoodCommitRequest_checkOK() throws Exception {
        final var postRequest = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.COMMIT);
        final var payload = objectMapper.writeValueAsString(postRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNotNull(handler.getTxRequest());
            assertEquals(postRequest.getTransaction(), handler.getTxRequest().getTransaction());
            assertEquals(postRequest.getOperation(), handler.getTxRequest().getOperation());
        }
    }

    @Test
    void givenGoodCancelRequest_checkOK() throws Exception {
        final var postRequest = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.CANCEL);
        final var payload = objectMapper.writeValueAsString(postRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNotNull(handler.getTxRequest());
            assertEquals(postRequest.getTransaction(), handler.getTxRequest().getTransaction());
            assertEquals(postRequest.getOperation(), handler.getTxRequest().getOperation());
        }
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        txMgr.getAndEraseTransaction(new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.CANCEL));
        transactionId = null;
        tokenMgr.removeToken(token);
        token = null;
        objectMapper = null;
        handler = null;
        txMgr = null;
        tokenMgr = null;
    }
}
