package comp4111.handler;

import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BooksDeleteHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private BooksDeleteHandler handler;
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new BooksDeleteHandler() {
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

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, BooksHandler.HANDLE_PATTERN + "/1?token=" + token);
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
            assertEquals(handler.getHandleMethod().toString(), response.getHeader("Allow").getValue());
        }
    }

    @Test
    void givenNoIdRequest_checkNotOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), BooksHandler.HANDLE_PATTERN + "?token=" + token);
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertNotEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    @Test
    void givenNoToken_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), BooksHandler.HANDLE_PATTERN + "/1");
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
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
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), BooksHandler.HANDLE_PATTERN + "/1?token=" + badToken);
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenBadBookId_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), BooksHandler.HANDLE_PATTERN + "/a?token=" + token);
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenGoodRequest_checksOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), BooksHandler.HANDLE_PATTERN + "/1?token=" + token);
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(1, handler.getBookId());
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
