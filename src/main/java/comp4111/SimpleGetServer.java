package comp4111;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Adapted from https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/ClassicFileServerExample.java
public class SimpleGetServer {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Lookup table for HTTP paths and their corresponding handlers.
     */
    private static final Map<String, HttpRequestHandler> patternHandler = Collections.unmodifiableMap(
            List.of(
                    new HttpRootHandler(),
                    new NotFoundHandler()
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
    static class SimpleExceptionListener implements ExceptionListener {

        @Override
        public void onError(Exception ex) {
            LOGGER.error(ex);
        }

        @Override
        public void onError(HttpConnection connection, Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                LOGGER.error("Connection timed out", ex);
            } else if (ex instanceof ConnectionClosedException) {
                LOGGER.error(ex);
            } else {
                LOGGER.error("Unknown exception occurred.", ex);
            }
        }
    }

    /**
     * An extension for {@link HttpRequestHandler} which also allows a class to specify the pattern it handles.
     */
    interface HttpPathHandler extends HttpRequestHandler {
        @NotNull
        String getHandlePattern();
    }

    /**
     * HTTP handler for responding to "/" path.
     */
    static class HttpRootHandler implements HttpPathHandler {

        @NotNull
        @Override
        public String getHandlePattern() {
            return "/";
        }

        @Override
        public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException {
            final String method = request.getMethod();
            if (!method.equals("GET")) {
                throw new MethodNotSupportedException("Method " + method + " not supported");
            }

            response.setCode(HttpStatus.SC_OK);
            final var entity = new StringEntity("<html><body><p>Hello World!</p></body></html>", ContentType.TEXT_HTML);
            response.setEntity(entity);
        }
    }

    /**
     * HTTP handler for responding to other paths which are not otherwise registered.
     */
    static class NotFoundHandler implements HttpPathHandler {

        @NotNull
        @Override
        public String getHandlePattern() {
            return "*";
        }

        @Override
        public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) {
            response.setCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}