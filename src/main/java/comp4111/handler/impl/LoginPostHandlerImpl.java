package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.LoginPostHandler;
import comp4111.model.LoginResult;
import comp4111.util.JacksonUtils;
import comp4111.util.SecurityUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.CompletionException;

public class LoginPostHandlerImpl extends LoginPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
//        LOGGER.trace("start handle");

        super.handleAsync(requestObject)
                .thenApplyAsync(request -> {
                    // TODO: userLoginAsync
                    if (!SecurityUtils.userLogin(request.getUsername(), request.getPassword())) {
                        throw new CompletionException("Bad login details", new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
                    }
                    return request;
                })
                .thenApplyAsync(request -> {
                    // TODO: newTokenAsync
                    return getTokenMgr().newToken(request.getUsername());
                })
                .thenApplyAsync(token -> {
                    if (token == null) {
                        throw new CompletionException("User already logged in", new HttpHandlingException(HttpStatus.SC_CONFLICT));
                    }
                    return token;
                })
                .thenApplyAsync(LoginResult::new)
                .thenApplyAsync(loginResult -> {
                    try {
                        return objectMapper.writeValueAsString(loginResult);
                    } catch (Throwable tr) {
                        throw new CompletionException("Error while serializing response", new HttpHandlingException(HttpStatus.SC_INTERNAL_SERVER_ERROR, tr));
                    }
                })
                .thenApplyAsync(json -> AsyncResponseBuilder.create(HttpStatus.SC_OK).setEntity(json, ContentType.APPLICATION_JSON).build())
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));

//        LOGGER.trace("handle done");
    }
}
