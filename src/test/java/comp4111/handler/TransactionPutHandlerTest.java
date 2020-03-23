package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import comp4111.controller.TransactionManager;
import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPutRequest;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TransactionPutHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private TransactionManager txMgr;
    private TransactionPutHandler handler;
    private ObjectMapper objectMapper;
    private String token;
    private UUID tx;

    @BeforeEach
    public void setUp() {
        super.setUp();

        handler = new TransactionPutHandler() {
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
        tx = txMgr.newTransaction();

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
    void givenNoPayloadRequest_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
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
                "\"Book\": 1," +
                "\"Action\": \"loan\"" +
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
    void givenMissingBookRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Action\": \"loan\"" +
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
    void givenMissingActionRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Book\": 1" +
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
                "\"Book\": 1," +
                "\"Action\": \"loan\"" +
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
    void givenNullBookRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Book\": null," +
                "\"Action\": \"loan\"" +
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
    void givenNullActionRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Book\": 1," +
                "\"Action\": null" +
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
    void givenBadTransactionRequest_checkBadRequest() throws Exception {
        final var badTx = txMgr.newTransaction();
        txMgr.getAndEraseTransaction(new TransactionPostRequest(badTx, TransactionPostRequest.Operation.CANCEL));

        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + badTx + "\", " +
                "\"Book\": 1," +
                "\"Action\": \"loan\"" +
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
    void givenBadBookRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Book\": \"abc\"," +
                "\"Action\": \"loan\"" +
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
    void givenBadActionRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": \"" + tx + "\", " +
                "\"Book\": 1," +
                "\"Action\": \"dance\"" +
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
    void givenGoodLoanRequest_checkOK() throws Exception {
        final var putRequest = new TransactionPutRequest(tx, 1, TransactionPutRequest.Action.LOAN);
        final var payload = objectMapper.writeValueAsString(putRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(putRequest.getTransaction(), handler.getPutRequest().getTransaction());
            assertEquals(putRequest.getId(), handler.getPutRequest().getId());
            assertEquals(putRequest.getAction(), handler.getPutRequest().getAction());
        }
    }

    @Test
    void givenGoodReturnRequest_checkOK() throws Exception {
        final var putRequest = new TransactionPutRequest(tx, 1, TransactionPutRequest.Action.RETURN);
        final var payload = objectMapper.writeValueAsString(putRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(putRequest.getTransaction(), handler.getPutRequest().getTransaction());
            assertEquals(putRequest.getId(), handler.getPutRequest().getId());
            assertEquals(putRequest.getAction(), handler.getPutRequest().getAction());
        }
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();

        txMgr.getAndEraseTransaction(new TransactionPostRequest(tx, TransactionPostRequest.Operation.CANCEL));
        tx = null;
        tokenMgr.removeToken(token);
        token = null;
        objectMapper = null;
        handler = null;
        txMgr = null;
        tokenMgr = null;
    }
}