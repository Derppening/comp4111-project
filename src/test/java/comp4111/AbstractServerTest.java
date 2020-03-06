package comp4111;

import comp4111.handler.HttpPathHandler;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.testing.classic.LoggingConnPoolListener;
import org.apache.hc.core5.testing.classic.LoggingExceptionListener;
import org.apache.hc.core5.testing.classic.LoggingHttp1StreamListener;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Abstract class for setting up required {@link HttpServer} and {@link HttpRequester} for testing.
 *
 * Usage: Inherit from this class, and call {@link AbstractServerTest#registerAndStartServer(HttpPathHandler...)} before
 * testing connections to the server.
 */
public abstract class AbstractServerTest {

    protected static final Timeout TIMEOUT = Timeout.ofSeconds(10);

    private ServerBootstrap serverBootstrap;
    protected HttpServer server;
    protected HttpRequester requester;

    @BeforeEach
    void setUp() {
        serverBootstrap = ServerBootstrap.bootstrap()
                .setSocketConfig(SocketConfig.custom().setSoTimeout(TIMEOUT).build())
                .setExceptionListener(LoggingExceptionListener.INSTANCE)
                .setStreamListener(LoggingHttp1StreamListener.INSTANCE);
        requester = RequesterBootstrap.bootstrap()
                .setSslContext(null)
                .setSocketConfig(SocketConfig.custom().setSoTimeout(TIMEOUT).build())
                .setMaxTotal(2)
                .setDefaultMaxPerRoute(2)
                .setStreamListener(LoggingHttp1StreamListener.INSTANCE)
                .setConnPoolListener(LoggingConnPoolListener.INSTANCE)
                .create();
    }

    /**
     * Registers a set of {@link HttpPathHandler} and starts the server.
     *
     * @param handlers Handlers to register to the server.
     */
    protected void registerAndStartServer(final HttpPathHandler... handlers) {
        Arrays.stream(handlers).forEach(handler -> serverBootstrap.register(handler.getHandlePattern(), handler));
        server = serverBootstrap.create();

        assertDoesNotThrow(() -> server.start());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close(CloseMode.IMMEDIATE);
        }
        server = null;
        if (requester != null) {
            try {
                requester.close(CloseMode.GRACEFUL);
            } catch (final Exception ignore) {
            }
        }
        serverBootstrap = null;
    }
}
