package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.impl.LoginPostHandlerImpl;
import comp4111.model.LoginRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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

    /**
     * Handles a request asynchronously.
     *
     * @param requestObject The request object to process.
     * @return A {@link java.util.concurrent.Future} object representing the required information.
     */
    protected CompletableFuture<LoginRequest> handleAsync(Message<HttpRequest, String> requestObject) {
        return CompletableFuture.completedFuture(requestObject)
                .thenApplyAsync(request -> {
                    checkMethodAsync(request);
                    return request;
                })
                .thenApplyAsync(HttpAsyncEndpointHandler::getPayloadAsync)
                .thenApplyAsync(payload -> {
                    try {
                        loginRequest = objectMapper.readValue(payload, LoginRequest.class);
                    } catch (Exception e) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
                    }
                    return loginRequest;
                })
                .thenApplyAsync(loginRequest -> {
                    LOGGER.info("POST /login Username=\"{}\" Password=\"{}\"", loginRequest.getUsername(), loginRequest.getPassword());
                    return loginRequest;
                });
    }

    @NotNull
    LoginRequest getLoginRequest() {
        return Objects.requireNonNull(loginRequest);
    }
}
