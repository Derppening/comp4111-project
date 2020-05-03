package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.LoginPostHandler;
import comp4111.model.LoginResult;
import comp4111.util.JacksonUtils;
import comp4111.util.SecurityUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class LoginPostHandlerImpl extends LoginPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!SecurityUtils.userLogin(getLoginRequest().getUsername(), getLoginRequest().getPassword())) {
            // The login is not successful (the username or password are invalid).
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
            return;
        }

        // The username and password are valid.
        final String token = getTokenMgr().newToken(getLoginRequest().getUsername());

        if (token == null) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_CONFLICT).build();
            responseTrigger.submitResponse(response, context);
            return;
        }

        final var loginResult = new LoginResult(token);

        final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_OK)
                .setEntity(objectMapper.writeValueAsString(loginResult), ContentType.APPLICATION_JSON).build();
        responseTrigger.submitResponse(response, context);
    }
}
