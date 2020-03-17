package comp4111;

import comp4111.handler.*;
import comp4111.listener.GenericExceptionListener;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    private static final Map<String, HttpPathHandler> PATTERN_HANDLER = List.of(
            new WildcardHandler(),
            new LoginPostHandler(),
            new LogoutGetHandler(),
            new BooksHandler(),
            new BookHandler(),
            new TransactionHandler()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpPathHandler::getHandlePattern, Function.identity()));

    public static void main(String[] args) {
        final var socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(10))
                .setTcpNoDelay(true)
                .build();

        final var serverBuilder = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .setExceptionListener(GenericExceptionListener.INSTANCE)
                .setSocketConfig(socketConfig);
        PATTERN_HANDLER.forEach(serverBuilder::register);

        final HttpServer server = serverBuilder.create();

        try {
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.close(CloseMode.GRACEFUL)));
            server.awaitTermination(TimeValue.MAX_VALUE);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Received unknown exception while running server", e);
        }
    }
}