package comp4111.example;

import comp4111.handler.HttpEndpoint;
import comp4111.handler.HttpEndpointHandler;
import comp4111.handler.HttpPath;
import comp4111.handler.HttpPathHandler;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Adapted from https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/ClassicFileServerExample.java
public class SimpleGetServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGetServer.class);

    /**
     * Lookup table for HTTP paths and their corresponding handlers.
     */
    private static final Map<String, HttpPathHandler> patternHandler = Collections.unmodifiableMap(
            List.of(
                    new NotFoundHandler(),
                    new HttpRootHandler()
            ).stream().collect(Collectors.toMap(HttpPathHandler::getHandlePattern, Function.identity())));

    public static void main(String[] args) {
        final var socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(10000))
                .setTcpNoDelay(true)
                .build();

        final var serverBuilder = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setExceptionListener(new SimpleExceptionListener())
                .setSocketConfig(socketConfig);
        patternHandler.forEach(serverBuilder::register);

        final HttpServer server = serverBuilder.create();

        try {
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.close(CloseMode.GRACEFUL)));
            server.awaitTermination(TimeValue.MAX_VALUE);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * A logger for any server errors.
     */
    private static class SimpleExceptionListener implements ExceptionListener {

        @Override
        public void onError(Exception ex) {
            LOGGER.error("", ex);
        }

        @Override
        public void onError(HttpConnection connection, Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                LOGGER.error("Connection timed out", ex);
            } else if (ex instanceof ConnectionClosedException) {
                LOGGER.trace("", ex);
            } else {
                LOGGER.error("Unknown exception occurred.", ex);
            }
        }
    }


    static class HttpRootHandler extends HttpPathHandler {
        @Override
        protected Map<Method, Supplier<HttpEndpointHandler>> getMethodLut() {
            return Map.of(Method.GET, HttpRootGetHandler::new);
        }

        @Override
        public @NotNull HttpPath getHandlerDefinition() {
            return new HttpPath() {

                @Override
                public @NotNull String getHandlePattern() {
                    return "/";
                }
            };
        }
    }

    /**
     * HTTP handler for responding to "/" path.
     */
    static class HttpRootGetHandler extends HttpEndpointHandler {

        @Override
        public @NotNull HttpEndpoint getHandlerDefinition() {
            return new HttpEndpoint() {
                @Override
                public @NotNull Method getHandleMethod() {
                    return Method.GET;
                }

                @Override
                public @NotNull String getHandlePattern() {
                    return "/";
                }
            };
        }

        @Override
        public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
            final Method method = HttpUtils.toMethodOrNull(request.getMethod());
            if (method == null || !method.equals(getHandleMethod())) {
                response.setCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                response.setHeader("Allow", getHandleMethod());
                return;
            }

            response.setCode(HttpStatus.SC_OK);
            final var entity = new StringEntity("<html><body><p>Hello World!</p></body></html>", ContentType.TEXT_HTML);
            response.setEntity(entity);
        }
    }

    /**
     * HTTP handler for responding to other paths which are not otherwise registered.
     */
    static class NotFoundHandler extends HttpPathHandler {

        @Override
        protected Map<Method, Supplier<HttpEndpointHandler>> getMethodLut() {
            return null;
        }

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
}
