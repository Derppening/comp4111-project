package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.AbstractServerTest;
import comp4111.model.LoginRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginPostHandlerTest extends AbstractServerTest {

    private LoginPostHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new LoginPostHandler() {
            @Override
            public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
                try {
                    super.handle(requestObject, responseTrigger, context);
                } catch (IllegalArgumentException e) {
                    return;
                }

                final var response = AsyncResponseBuilder.create(HttpStatus.SC_OK).build();
                responseTrigger.submitResponse(response, context);
            }
        };
        objectMapper = JacksonUtils.getDefaultObjectMapper();

        registerAndStartServer(handler);
    }

    @Test
    void givenBadMethodRequest_checkMethodNotAllowed() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, handler.getHandlePattern());
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
            assertEquals(handler.getHandleMethod().toString(), response.getHeader("Allow").getValue());
        }
    }

    @Test
    void givenNoPayloadRequest_checkBadRequest() throws Exception {
        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenEmptyPayloadRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingUsernameRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{\"Password\": \"password001\"}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenMissingPasswordRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{\"Username\": \"username001\"}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullUsernameRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{\"Username\": null, \"Password\": \"password001\"}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenNullPasswordRequest_checkBadRequest() throws Exception {
        @Language("JSON") final var payload = "{\"Username\": \"username001\", \"Password\": null}";

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void givenGoodRequest_checkOK() throws Exception {
        final var loginRequest = new LoginRequest("username001", "password001");
        @Language("JSON") final var payload = objectMapper.writeValueAsString(loginRequest);

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(handler.getHandleMethod(), handler.getHandlePattern());
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        try (final var response = requester.execute(target, request, SERVER_TIMEOUT, context)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
            assertEquals(loginRequest.getUsername(), handler.getLoginRequest().getUsername());
            assertEquals(loginRequest.getPassword(), handler.getLoginRequest().getPassword());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        objectMapper = null;
        handler = null;
    }
}
