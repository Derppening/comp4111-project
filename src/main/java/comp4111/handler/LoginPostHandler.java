package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.LoginPostHandlerImpl;
import comp4111.model.LoginRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * Endpoint handler for all {@code /login} POST requests.
 */
public abstract class LoginPostHandler extends HttpAsyncEndpointHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/login";
    private static final HttpEndpoint HANDLER_DEFINITION = new HttpEndpoint() {

        @NotNull
        @Override
        public String getHandlePattern() {
            return HANDLE_PATTERN;
        }

        @NotNull
        @Override
        public Method getHandleMethod() {
            return Method.POST;
        }
    };

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Nullable
    private LoginRequest loginRequest;

    @NotNull
    public static LoginPostHandler getInstance() {
        return new LoginPostHandlerImpl();
    }

    @NotNull
    @Override
    public final HttpEndpoint getHandlerDefinition() {
        return HANDLER_DEFINITION;
    }

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context)
            throws HttpException, IOException {
        checkMethod(requestObject, responseTrigger, context);

        final AsyncResponseProducer response;
        final var payload = getPayload(requestObject, responseTrigger, context);

        try {
            loginRequest = objectMapper.readValue(payload, LoginRequest.class);
        } catch (Exception e) {
            response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST)
                    .setEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML).build();
            responseTrigger.submitResponse(response, context);
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("POST /login Username=\"{}\" Password=\"{}\"", loginRequest.getUsername(), loginRequest.getPassword());
    }

    @NotNull
    protected LoginRequest getLoginRequest() {
        return Objects.requireNonNull(loginRequest);
    }
}
