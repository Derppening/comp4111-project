package comp4111.example;

import comp4111.util.HttpUtils;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.NoopEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Adapted from https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncFileServerExample.java
public class SimpleAsyncGetServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAsyncGetServer.class);

    /**
     * Lookup table for HTTP paths and their corresponding handlers.
     */
    private static final Map<String, AsyncServerRequestHandler<Message<HttpRequest, Void>>> PATTERN_HANDLER = Map.of(
            "*", new NotFoundHandler(),
            "/", new HttpRootHandler()
    );

    public static void main(String[] args) {
        final var config = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(10000))
                .setTcpNoDelay(true)
                .build();

        final var serverBuilder = AsyncServerBootstrap.bootstrap()
                .setIOReactorConfig(config)
                .setExceptionCallback(new SimpleExceptionCallback());
        PATTERN_HANDLER.forEach(serverBuilder::register);

        final var server = serverBuilder.create();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("HTTP server shutting down");
            server.close(CloseMode.GRACEFUL);
        }));

        server.start();
        try {
            final var future = server.listen(new InetSocketAddress(8080));
            final var listenerEndpoint = future.get();
            System.out.println("Listening on " + listenerEndpoint.getAddress());
            server.awaitShutdown(TimeValue.MAX_VALUE);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logger for any server errors.
     */
    private static class SimpleExceptionCallback implements Callback<Exception> {

        @Override
        public void execute(Exception object) {
            if (object instanceof SocketTimeoutException) {
                LOGGER.error("Connection timed out", object);
            } else if (object instanceof ConnectionClosedException) {
                LOGGER.trace("", object);
            } else {
                LOGGER.error("Unknown exception occurred.", object);
            }
        }
    }

    /**
     * HTTP handler for responding to "/" path.
     */
    static class HttpRootHandler implements AsyncServerRequestHandler<Message<HttpRequest, Void>> {

        @Override
        public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws HttpException {
            return new BasicRequestConsumer<>(entityDetails != null ? new NoopEntityConsumer() : null);
        }

        @Override
        public void handle(Message<HttpRequest, Void> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
            final AsyncResponseProducer response;

            final Method method = HttpUtils.toMethodOrNull(requestObject.getHead().getMethod());
            LOGGER.debug("{} {}", method != null ? method : requestObject.getHead().getMethod(), requestObject.getHead().getPath());

            if (method == null || !method.equals(Method.GET)) {
                response = AsyncResponseBuilder.create(HttpStatus.SC_METHOD_NOT_ALLOWED)
                        .setHeader(HttpHeaders.ACCEPT, Method.GET.toString())
                        .build();
            } else {
                response = AsyncResponseBuilder.create(HttpStatus.SC_OK)
                        .setEntity("<html><body><p>Hello World!</p></body></html>", ContentType.TEXT_HTML)
                        .build();
            }
            responseTrigger.submitResponse(response, context);
        }
    }

    /**
     * HTTP handler for responding to other paths which are not otherwise registered.
     */
    static class NotFoundHandler implements AsyncServerRequestHandler<Message<HttpRequest, Void>> {

        @Override
        public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws HttpException {
            return new BasicRequestConsumer<>(entityDetails != null ? new NoopEntityConsumer() : null);
        }

        @Override
        public void handle(Message<HttpRequest, Void> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
            final Method method = HttpUtils.toMethodOrNull(requestObject.getHead().getMethod());
            LOGGER.debug("{} {}", method != null ? method : requestObject.getHead().getMethod(), requestObject.getHead().getPath());

            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_NOT_FOUND).build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
