package comp4111.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BookHandler extends HttpPathHandler {

    public static final String HANDLE_PATTERN = "/book/*";
    private static final Map<Method, HttpEndpointHandler> METHOD_LUT = Collections.unmodifiableMap(
            List.of(
                    new BookPutHandler(),
                    new BookDeleteHandler()
            ).stream().collect(Collectors.toMap(HttpEndpointHandler::getHandleMethod, Function.identity())));

    @NotNull
    @Override
    public HttpPath getHandlerDefinition() {
        return new HttpPath() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return HANDLE_PATTERN;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final Method method = toMethodOrNull(request.getMethod());

        HttpEndpointHandler handler = null;
        if (method != null) {
            handler = METHOD_LUT.get(method);
        }

        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", METHOD_LUT.keySet().stream().map(Enum::toString).collect(Collectors.joining(",")));
        }
    }

    static long getIdFromRequest(@NotNull String path) {
        final var startIdx = "/book/".length();
        final var endIdx = path.indexOf('?') != -1 ? path.indexOf('?') : path.length();

        return Long.parseLong(path.substring(startIdx, endIdx));
    }
}

final class BookPutHandler extends HttpEndpointHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @Override
            public @NotNull String getHandlePattern() {
                return BookHandler.HANDLE_PATTERN;
            }

            @Override
            public @NotNull Method getHandleMethod() {
                return Method.PUT;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        final var token = queryParams.get("token");

        final var bookId = BookHandler.getIdFromRequest(request.getPath());

        final var payload = request.getEntity().getContent().readAllBytes();

        final boolean available;
        try {
            final var rootNode = objectMapper.readTree(payload);
            available = rootNode.get("Available").asBoolean();
        } catch (Exception e) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(new StringEntity(e.getLocalizedMessage(), ContentType.TEXT_HTML));
            return;
        }

        LOGGER.info("PUT /book token={} id={} Available={}", token, bookId, available);

        // TODO: Handle payload

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}

final class BookDeleteHandler extends HttpEndpointHandler {

    @Override
    public @NotNull HttpEndpoint getHandlerDefinition() {
        return new HttpEndpoint() {
            @NotNull
            @Override
            public String getHandlePattern() {
                return BookHandler.HANDLE_PATTERN;
            }

            @NotNull
            @Override
            public Method getHandleMethod() {
                return Method.DELETE;
            }
        };
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        final var queryParams = parseQueryParams(request.getPath());
        if (!queryParams.containsKey("token")) {
            response.setCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        final var token = queryParams.get("token");

        final var bookId = BookHandler.getIdFromRequest(request.getPath());

        LOGGER.info("DELETE /book token=\"{}\" id={}", token, bookId);

        // TODO(Derppening): Handle

        response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }
}
