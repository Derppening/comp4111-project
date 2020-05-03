package comp4111.dal;

import comp4111.function.ConnectionFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

public class DatabaseConnectionPoolV2 implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionPoolV2.class);

    /**
     * The URL to the MySQL database.
     */
    public static final String MYSQL_URL;
    /**
     * The username used to login.
     */
    public static final String MYSQL_LOGIN;
    /**
     * The password of the user.
     */
    public static final String MYSQL_PASSWORD;
    /**
     * The name of the database.
     */
    public static final String DB_NAME;

    private static final long GC_CONNECTION_EVICT_TIME = 60_000;
    private static final long GC_PERIOD = 30_000;

    static {
        final var classLoader = Thread.currentThread().getContextClassLoader();
        final var mysqlProperties = new Properties();
        try {
            mysqlProperties.load(classLoader.getResourceAsStream("mysql.properties"));
        } catch (IOException e) {
            LOGGER.error("Cannot read from database properties", e);
            System.exit(1);
        }

        MYSQL_URL = mysqlProperties.get("mysql.url").toString();
        MYSQL_LOGIN = mysqlProperties.get("mysql.username").toString();
        MYSQL_PASSWORD = mysqlProperties.get("mysql.password").toString();
        DB_NAME = mysqlProperties.get("mysql.database").toString();
    }

    private static DatabaseConnectionPoolV2 INSTANCE = null;

    private final String url;
    private final String login;
    private final String password;
    private final String db;

    private final Set<DatabaseConnectionV2> pool = new HashSet<>();
    private final Timer gcPoolTimer = new Timer();

    {
        gcPoolTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final var now = Instant.now().toEpochMilli();
                synchronized (pool) {
                    final var initSize = pool.size();

                    pool.stream()
                            .filter(con -> {
                                final var lastUsedTime = con.getLastUsedTime();
                                return lastUsedTime != null && now - lastUsedTime.toEpochMilli() > GC_CONNECTION_EVICT_TIME;
                            })
                            .forEach(it -> {
                                try {
                                    it.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                    pool.removeIf(DatabaseConnectionV2::isClosed);

                    final var finalSize = pool.size();

                    if (finalSize != initSize) {
                        LOGGER.info("Completed connection pool eviction: {} -> {} connections", initSize, finalSize);
                    }
                }
            }
        }, GC_PERIOD, GC_PERIOD);
    }

    public synchronized static DatabaseConnectionPoolV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseConnectionPoolV2(MYSQL_URL, DB_NAME, MYSQL_LOGIN, MYSQL_PASSWORD);
        }

        return INSTANCE;
    }

    DatabaseConnectionPoolV2(@NotNull String url, @NotNull String database, @NotNull String username, @NotNull String password) {
        this.url = url;
        this.login = username;
        this.password = password;
        this.db = database;
    }

    @NotNull
    private DatabaseConnectionV2 newConnection() {
        try {
            final var connection = new DatabaseConnectionV2(url, db, login, password);
            synchronized (pool) {
                pool.add(connection);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create new connection to pool", e);
        }
    }

    @NotNull
    private synchronized DatabaseConnectionV2 findOrNewConnection() throws SQLException {
        final DatabaseConnectionV2 connection;
        synchronized (pool) {
            if (pool.isEmpty()) {
                connection = newConnection();
            } else {
                try {
                    connection = pool.stream()
                            .filter(con -> !con.isInUse())
                            .findAny()
                            .orElseGet(this::newConnection);
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof SQLException) {
                        throw (SQLException) e.getCause();
                    } else {
                        throw e;
                    }
                }
            }
        }
        return connection;
    }

    @Nullable
    private DatabaseConnectionV2 findConnection(@NotNull Predicate<DatabaseConnectionV2> predicate) {
        synchronized (pool) {
            return pool.stream()
                    .filter(predicate)
                    .findFirst()
                    .orElse(null);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T execStmt(@NotNull ConnectionFunction block) throws SQLException {
        final var connection = findOrNewConnection();

        return (T) connection.execStmt(block);
    }

    public long getIdForTransaction(int timeout) throws SQLException {
        final var connection = findOrNewConnection();
        return connection.getIdForTransaction(timeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T putTransactionWithId(long id, @NotNull ConnectionFunction block) throws SQLException {
        final var connection = findConnection(it -> it.isInUse() && it.getTransactionId() == id);

        if (connection != null) {
            return (T) connection.execTransaction(block);
        } else {
            return null;
        }
    }

    public boolean executeTransaction(long id, boolean shouldCommit) throws SQLException {
        final var connection = findConnection(it -> it.isInUse() && it.getTransactionId() == id);

        if (connection != null) {
            if (shouldCommit) {
                return connection.commit();
            } else {
                connection.rollback();
            }
        }
        return false;
    }

    @Override
    public void close() {
        pool.forEach(con -> {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        pool.clear();
    }
}
