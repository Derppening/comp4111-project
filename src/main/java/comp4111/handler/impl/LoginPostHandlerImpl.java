package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.controller.TokenManager;
import comp4111.handler.LoginPostHandler;
import comp4111.model.LoginResult;
import comp4111.util.JacksonUtils;
import comp4111.util.LoginUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class LoginPostHandlerImpl extends LoginPostHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();
    private final TokenManager tokenMgr = TokenManager.getInstance();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!LoginUtils.userLogin(loginRequest.getUsername(), loginRequest.getPassword())) {
            // The login is not successful (the username and password are invalid).
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        // The username and password are valid.
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

        response.setCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(objectMapper.writeValueAsString(loginResult), ContentType.APPLICATION_JSON));
    }
}
