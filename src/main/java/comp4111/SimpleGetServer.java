package comp4111;

import org.apache.http.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

// Adapted from https://hc.apache.org/httpcomponents-core-ga/httpcore/examples/org/apache/http/examples/HttpFileServer.java
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
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(10000)
                .setTcpNoDelay(true)
                .build();

        final ServerBootstrap serverBuilder = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setServerInfo("HTTP/1.1")
                .setExceptionLogger(new StdErrorExceptionLogger())
                .setSocketConfig(socketConfig);
        patternHandler.forEach(serverBuilder::registerHandler);

        final HttpServer server = serverBuilder.create();

        try {
            server.start();
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown(5, TimeUnit.SECONDS)));
    }

    /**
     * A logger for any server errors.
     */
    static class StdErrorExceptionLogger implements ExceptionLogger {
        @Override
        public void log(final Exception ex) {
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
        /* notnull */
        String getHandlePattern();
    }

    /**
     * HTTP handler for responding to "/" path.
     */
    static class HttpRootHandler implements HttpPathHandler {

        @Override
        public String getHandlePattern() {
            return "/";
        }

        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException {
            final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET")) {
                throw new MethodNotSupportedException("Method " + method + " not supported");
            }

            response.setStatusCode(HttpStatus.SC_OK);
            final StringEntity entity = new StringEntity("<html><body><p>Hello World!</p></body></html>", ContentType.TEXT_HTML);
            response.setEntity(entity);
        }
    }

    /**
     * HTTP handler for responding to other paths which are not otherwise registered.
     */
    static class NotFoundHandler implements HttpPathHandler {

        @Override
        public String getHandlePattern() {
            return "*";
        }

        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
        }
    }
}
