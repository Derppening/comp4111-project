package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.impl.LoginPostHandlerImpl;
import comp4111.model.LoginRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /login} POST requests.
 */
public abstract class LoginPostHandler extends HttpEndpointHandler {

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
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        checkMethod(request, response);

        final var payload = getPayload(request, response);

        try {
            loginRequest = objectMapper.readValue(payload, LoginRequest.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            throw new IllegalArgumentException(e);
        }

        LOGGER.info("POST /login Username=\"{}\" Password=\"{}\"", loginRequest.getUsername(), loginRequest.getPassword());
    }

    protected LoginRequest getLoginRequest() {
        return loginRequest;
    }
}
