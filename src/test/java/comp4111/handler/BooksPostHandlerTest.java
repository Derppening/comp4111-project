package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.AbstractServerTest;
import comp4111.controller.TokenManager;
import comp4111.model.Book;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BooksPostHandlerTest extends AbstractServerTest {

    private TokenManager tokenMgr;
    private BooksPostHandler handler;
    private ObjectMapper objectMapper;
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new BooksPostHandler() {
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

        objectMapper = new ObjectMapper();

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, handler.getHandlePattern());
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
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
    void givenNoPayloadRequest_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
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
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingTitleRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingAuthorRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingPublisherRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingYearRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": \"Macmillan Publishers\"" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullTitleRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": null, " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullAuthorRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": null, " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullPublisherRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": null, " +
                "\"Year\": 1865" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullYearRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": null" +
                "}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenGoodRequest_checkOK() throws Exception {
        final var book = new Book(
                "Alice in Wonderland",
                "Lewis Carroll",
                "Macmillan Publishers",
                1865
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern() + "?token=" + token);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, CLIENT_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(book.getTitle(), handler.getRequest().book.getTitle());
            assertEquals(book.getAuthor(), handler.getRequest().book.getAuthor());
            assertEquals(book.getPublisher(), handler.getRequest().book.getPublisher());
            assertEquals(book.getYear(), handler.getRequest().book.getYear());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        tokenMgr.removeToken(token);
        token = null;
        objectMapper = null;
        handler = null;
        tokenMgr = null;
    }
}
