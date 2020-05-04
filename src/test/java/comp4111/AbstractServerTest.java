package comp4111;

import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.handler.HttpAsyncPathHandler;
import comp4111.handler.HttpEndpointHandler;
import comp4111.handler.HttpPathHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.impl.bootstrap.*;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.testing.classic.LoggingConnPoolListener;
import org.apache.hc.core5.testing.classic.LoggingHttp1StreamListener;
import org.apache.hc.core5.testing.nio.LoggingExceptionCallback;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Abstract class for setting up required {@link HttpServer} and {@link HttpRequester} for testing.
 *
 * Usage: Inherit from this class, and call {@link AbstractServerTest#registerAndStartServer(HttpAsyncPathHandler...)}
 * before testing connections to the server. If {@link BeforeEach} and {@link AfterEach} is used in the overriding
 * class, call {@link AbstractServerTest#setUp()} and {@link AbstractServerTest#tearDown()} respectively.
 */
public abstract class AbstractServerTest {

    protected static final Timeout CLIENT_TIMEOUT = Timeout.DISABLED;
    protected static final Timeout SERVER_TIMEOUT = Timeout.ofSeconds(10);

    private AsyncServerBootstrap serverBootstrap;
    protected HttpAsyncServer server;
    protected HttpRequester requester;

    private HttpRequester createDefaultRequester() {
        return createDefaultRequester(CLIENT_TIMEOUT);
    }

    private HttpRequester createDefaultRequester(Timeout socketTimeout) {
        return RequesterBootstrap.bootstrap()
                .setSslContext(null)
                .setSocketConfig(SocketConfig.custom().setSoTimeout(socketTimeout).build())
                .setMaxTotal(2)
                .setDefaultMaxPerRoute(2)
                .setStreamListener(LoggingHttp1StreamListener.INSTANCE)
                .setConnPoolListener(LoggingConnPoolListener.INSTANCE)
                .create();
    }

    @BeforeEach
    public void setUp() throws Exception {
        final var reactorConfig = IOReactorConfig.custom()
                .setSoReuseAddress(true)
                .setSoTimeout(SERVER_TIMEOUT)
                .setTcpNoDelay(true)
                .build();
        serverBootstrap = AsyncServerBootstrap.bootstrap()
                .setExceptionCallback(LoggingExceptionCallback.INSTANCE)
                .setStreamListener(org.apache.hc.core5.testing.nio.LoggingHttp1StreamListener.INSTANCE_SERVER)
                .setIOReactorConfig(reactorConfig);
        requester = createDefaultRequester(SERVER_TIMEOUT);
    }

    /**
     * Registers a set of {@link HttpPathHandler} and starts the server.
     *
     * @param handlers Handlers to register to the server.
     */
    protected void registerAndStartServer(final HttpAsyncPathHandler... handlers) {
        assumeFalse(server != null);

        Arrays.stream(handlers).forEach(handler -> serverBootstrap.register(handler.getHandlePattern(), handler));
        server = serverBootstrap.create();

        assertDoesNotThrow(() -> {
            server.start();
            server.listen(new InetSocketAddress(8080)).get();
        });
    }

    /**
     * Registers a set of {@link HttpEndpointHandler} and starts the server.
     *
     * @param handlers Handlers to register to the server.
     */
    protected void registerAndStartServer(final HttpAsyncEndpointHandler... handlers) {
        assumeFalse(server != null);

        Arrays.stream(handlers).forEach(handler -> serverBootstrap.register(handler.getHandlePattern(), handler));
        server = serverBootstrap.create();

        assertDoesNotThrow(() -> {
            server.start();
            server.listen(new InetSocketAddress(8080)).get();
        });
    }

    protected HttpHost getDefaultHttpHost(HttpAsyncServer server) {
        final var endpoint = (InetSocketAddress) server.getEndpoints()
                .stream()
                .findAny()
                .orElseThrow()
                .getAddress();
        return new HttpHost(URIScheme.HTTP.toString(), endpoint.getHostString(), endpoint.getPort());
    }

    protected ClassicHttpResponse makeRequest(Method method, String path, @Nullable HttpEntity entity) throws HttpException, IOException {
        final var requester = createDefaultRequester();

        final var target = getDefaultHttpHost(server);
        final var context = HttpCoreContext.create();
        final ClassicHttpRequest request = new BasicClassicHttpRequest(method, path);
        request.setEntity(entity);

        return requester.execute(target, request, SERVER_TIMEOUT, context);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.close(CloseMode.IMMEDIATE);
            server.close();
        }
        server = null;
        if (requester != null) {
            requester.close(CloseMode.IMMEDIATE);
            requester.close();
        }
        serverBootstrap = null;
    }
}
