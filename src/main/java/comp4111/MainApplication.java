package comp4111;

import comp4111.dal.DatabaseConnectionPoolV2;
import comp4111.dal.DatabaseUtils;
import comp4111.handler.*;
import comp4111.listener.GenericExceptionCallback;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    public static final Map<String, HttpAsyncPathHandler> PATTERN_HANDLER = List.of(
            WildcardHandler.getInstance(),
            LoginHandler.getInstance(),
            LogoutHandler.getInstance(),
            BooksHandler.getInstance(),
            TransactionHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpAsyncPathHandler::getHandlePattern, Function.identity()));

    public static void main(String[] args) {
        boolean recreateTables = Arrays.asList(args).contains("--recreate-tables");
        boolean recreateDb = Arrays.asList(args).contains("--recreate-db");

        final var config = IOReactorConfig.custom()
                .setSoKeepAlive(false)
                .setSoReuseAddress(true)
                .setSoTimeout(Timeout.DISABLED)
                .setTcpNoDelay(true)
                .build();

        final var serverBuilder = AsyncServerBootstrap.bootstrap()
                .setExceptionCallback(GenericExceptionCallback.INSTANCE)
                .setIOReactorConfig(config);
        PATTERN_HANDLER.forEach(serverBuilder::register);

        final HttpAsyncServer server = serverBuilder.create();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("HTTP server shutting down");
            server.close(CloseMode.GRACEFUL);
        }));

        try {
            // Set up the database connection.
            if (recreateDb) {
                DatabaseUtils.createDatabaseSchema(true);
            }
            if (recreateDb || recreateTables) {
                DatabaseUtils.createTableSchemas(true);
            }
            DatabaseUtils.createDefaultUsers();

            DatabaseConnectionPoolV2.getInstance().setDefaultLockTimeout(Duration.ofSeconds(3));

            server.start();
            final var future = server.listen(new InetSocketAddress(8080));
            final var listenerEndpoint = future.get();
            LOGGER.info("Listening on " + listenerEndpoint.getAddress());
            server.awaitShutdown(TimeValue.MAX_VALUE);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Received unknown exception while running server", e);
        }
    }
}
