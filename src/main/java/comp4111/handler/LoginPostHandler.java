package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.model.LoginRequest;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Endpoint handler for all {@code /login} POST requests.
 */
public final class LoginPostHandler extends HttpEndpointHandler {

    public static final String HANDLE_PATTERN = PATH_PREFIX + "/login";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @NotNull
    @Override
    public HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {

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
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());
        if (method == null || !method.equals(getHandleMethod())) {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", getHandleMethod());
            return;
        }

        // TODO: Handle null payload
        final var payload = request.getEntity().getContent().readAllBytes();

        final LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(payload, LoginRequest.class);
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            return;
        }

        if (loginRequest.getUsername().isBlank() || loginRequest.getPassword().isBlank()) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity("Username/Password must be provided", ContentType.TEXT_HTML));
            return;
        }

        LOGGER.info("POST /login Username=\"{}\" Password=\"{}\"", loginRequest.getUsername(), loginRequest.getPassword());

        // TODO(Derppening): Handle payload

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}
