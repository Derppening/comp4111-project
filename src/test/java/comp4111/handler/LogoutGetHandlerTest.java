package comp4111.handler;

import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogoutGetHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private LogoutGetHandler handler;
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new LogoutGetHandler() {
            @Override
            public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {
                super.handleAsync(requestObject)
                        .thenApplyAsync(json -> AsyncResponseBuilder.create(HttpStatus.SC_OK).build())
                        .exceptionally(this::exceptionToResponse)
                        .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
            }
        };
        tokenMgr = TokenManager.getInstance();
        token = tokenMgr.newToken("user001");

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.POST, handler.getHandlePattern() + "?token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
            assertEquals(handler.getHandleMethod().toString(), response.getHeader("Allow").getValue());
        }
    }

    @Test
    void givenNoTokenRequest_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenGoodRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(token, handler.getToken());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        tokenMgr.removeToken(token);
        token = null;
        handler = null;
        tokenMgr = null;
    }
}
