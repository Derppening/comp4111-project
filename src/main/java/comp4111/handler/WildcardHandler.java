package comp4111.handler;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

public class WildcardHandler extends HttpPathHandler {

    @Override
    public @NotNull HttpPath getHandlerDefinition() {
        return new HttpPath() {

            @Override
            public @NotNull String getHandlePattern() {
                return "*";
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
        response.setCode(HttpStatus.SC_NOT_FOUND);
    }
}
