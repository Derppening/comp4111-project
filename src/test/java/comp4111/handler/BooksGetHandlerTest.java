package comp4111.handler;

import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BooksGetHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private BooksGetHandler handler;
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new BooksGetHandler() {
            @Override
            protected @NotNull TokenManager getTokenMgr() {
                return tokenMgr;
            }

            @Override
            public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {
                super.handleAsync(requestObject)
                        .thenApplyAsync(json -> AsyncResponseBuilder.create(HttpStatus.SC_OK).build())
                        .exceptionally(this::exceptionToResponse)
                        .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
            }
        };
        tokenMgr = TokenManager.getInstance();
        token = tokenMgr.newToken("user00001");

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        try (final var response = makeRequest(Method.POST, handler.getHandlePattern(), null)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
            assertEquals(handler.getHandleMethod().toString(), response.getHeader("Allow").getValue());
        }
    }

    @Test
    void givenNoToken_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenBadToken_checkBadRequest() throws Exception {
        final var badToken = tokenMgr.newToken("user00002");
        assumeTrue(badToken != null);
        assumeTrue(tokenMgr.removeToken(badToken));

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + badToken);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenStandardRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    @Test
    void givenBookIdRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?id=1&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(1, handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    @Test
    void givenBookTitleRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?title=Alice&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertEquals("Alice", handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    @Test
    void givenBookAuthorRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?author=Lewis&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertEquals("Lewis", handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    @Test
    void givenBookLimitRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?limit=10&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertEquals(10, handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    // TODO(Derppening): Convert to Sort id, Sort title, Sort author, BadSort test cases
    @Test
    void givenBookSortRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?sortby=id&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.ID, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    // TODO(Derppening): Convert to Order asc, Order desc, BadOrder test cases
    @Test
    void givenBookOrderRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?order=asc&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.ASC, handler.getQueryParams().order);
        }
    }

    @Test
    void givenCompoundAuthorIdRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?author=Lewis&id=5&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(5, handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertEquals("Lewis", handler.getQueryParams().author);
            assertNull(handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.NONE, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.NONE, handler.getQueryParams().order);
        }
    }

    @Test
    void givenCompoundLimitSortbyOrderRequest_checkOK() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?limit=10&sortby=id&order=desc&token=" + token);
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertNull(handler.getQueryParams().id);
            assertNull(handler.getQueryParams().title);
            assertNull(handler.getQueryParams().author);
            assertEquals(10, handler.getQueryParams().limit);
            assertEquals(BooksGetHandler.QueryParams.SortField.ID, handler.getQueryParams().sort);
            assertEquals(BooksGetHandler.QueryParams.OutputOrder.DESC, handler.getQueryParams().order);
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
