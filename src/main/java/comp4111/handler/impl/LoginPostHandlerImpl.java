package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.handler.LoginPostHandler;
import comp4111.model.LoginResult;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class LoginPostHandlerImpl extends LoginPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        // TODO: Handle login request
        LOGGER.warn("PLACEHOLDER: Assuming that login combination is correct");

        final var tokenMgr = getTokenMgr();
        final String token;
        synchronized (tokenMgr) {
            if (tokenMgr.containsUser(getLoginRequest().getUsername())) {
                token = null;
            } else {
                token = tokenMgr.newToken(getLoginRequest().getUsername());
            }
        }

        if (token == null) {
            response.setCode(HttpStatus.SC_CONFLICT);
            return;
        }

        final var loginResult = new LoginResult(token);

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        response.setEntity(new StringEntity(objectMapper.writeValueAsString(loginResult), ContentType.APPLICATION_JSON));
    }
}
