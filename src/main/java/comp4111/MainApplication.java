package comp4111;

import comp4111.dal.DatabaseConnection;
import comp4111.dal.DatabaseConnectionPoolV2;
import comp4111.dal.LoginDataAccess;
import comp4111.handler.*;
import comp4111.listener.GenericExceptionCallback;
import comp4111.util.SecurityUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    public static final Map<String, AsyncServerRequestHandler<Message<HttpRequest, String>>> PATTERN_HANDLER = List.of(
            WildcardHandler.getInstance(),
            LoginHandler.getInstance(),
            LogoutHandler.getInstance()
//            BooksHandler.getInstance(),
//            TransactionHandler.getInstance()
    ).stream().collect(Collectors.toUnmodifiableMap(HttpAsyncPathHandler::getHandlePattern, Function.identity()));

    public static void main(String[] args) {
        final var config = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(10))
                .setTcpNoDelay(true)
                .build();

        final var serverBuilder = AsyncServerBootstrap.bootstrap()
                .setExceptionCallback(GenericExceptionCallback.INSTANCE)
                .setIOReactorConfig(config);
        PATTERN_HANDLER.forEach(serverBuilder::register);

        final HttpAsyncServer server = serverBuilder.create();
        DatabaseConnectionPoolV2.getInstance();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("HTTP server shutting down");
            server.close(CloseMode.GRACEFUL);
        }));

        try {
            // Set up the database connection.
            DatabaseConnection.setConfig();
            createDefaultUsers();

            server.start();
            final var future = server.listen(new InetSocketAddress(8080));
            final var listenerEndpoint = future.get();
            LOGGER.info("Listening on " + listenerEndpoint.getAddress());
            server.awaitShutdown(TimeValue.MAX_VALUE);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Received unknown exception while running server", e);
        } finally {
            DatabaseConnection.cleanUp();
            LOGGER.info("The database connection is closed");
        }
    }

    /**
     * A helper function.
     */
    public static void createDefaultUsers() {
        if (!SecurityUtils.userLogin("user001", "pass001")) { // The database probably does not contain user credentials.
            for (int i = 1; i < 101; i++) {
                String suffix = String.format("%03d", i);
                LoginDataAccess.createUserAccount("user" + suffix, "pass" + suffix);
            }
            LOGGER.info("The user accounts are recreated");
        }
    }
}
