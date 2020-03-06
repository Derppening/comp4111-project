package comp4111.example;

import comp4111.AbstractServerTest;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleGetServerTest extends AbstractServerTest {

    @Test
    void givenRootGetRequest_checkResponse() throws Exception {
        final var rootHandler = new SimpleGetServer.HttpRootHandler();
        registerAndStartServer(rootHandler);

        final var target = new HttpHost(URIScheme.HTTP.id, "localhost", server.getLocalPort());
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, rootHandler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            final var body = EntityUtils.toString(response.getEntity());
            assertTrue(body.contains("Hello World!"));
        }
    }

    @Test
    void givenRootPostRequest_checkResponse() throws Exception {
        final var rootHandler = new SimpleGetServer.HttpRootHandler();
        registerAndStartServer(rootHandler);

        final var target = new HttpHost(URIScheme.HTTP.id, "localhost", server.getLocalPort());
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.POST, rootHandler.getHandlePattern());
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
        }
    }

    @Test
    void givenUnsupportedGetRequest_checkResponse() throws Exception {
        final var notFoundHandler = new SimpleGetServer.NotFoundHandler();
        registerAndStartServer(notFoundHandler);

        final var target = new HttpHost(URIScheme.HTTP.id, "localhost", server.getLocalPort());
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, "/unknown-path");
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
        }
    }

    @Test
    void givenUnsupportedPostRequest_checkResponse() throws Exception {
        final var notFoundHandler = new SimpleGetServer.NotFoundHandler();
        registerAndStartServer(notFoundHandler);

        final var target = new HttpHost(URIScheme.HTTP.id, "localhost", server.getLocalPort());
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.POST, "/unknown-path");
        try (final var response = requester.execute(target, request, TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
        }
    }
}
