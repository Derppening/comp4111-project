package comp4111.dal;

import comp4111.function.ConnectionFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * A connection pool for grouping connections into one SQL server.
 *
 * All SQL connection properties are read from {@code mysql.properties} in the resources directory.
 */
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

    /**
     * How long after a connection is unused should the garbage collector of the pool remove the connection from the
     * pool.
     */
    @NotNull
    private static final Duration GC_CONNECTION_EVICT_TIME = Duration.ofSeconds(60);
    /**
     * How often the garbage collector of the pool should run.
     */
    @NotNull
    private static final Duration GC_PERIOD = Duration.ofSeconds(30);

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

    /**
     * The pool of connection instances.
     */
    private final Set<DatabaseConnectionV2> pool = new HashSet<>();
    /**
     * Times for scheduling garbage collection tasks.
     */
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
                                return !con.isInUse() && now - lastUsedTime.toEpochMilli() > GC_CONNECTION_EVICT_TIME.toMillis();
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
        }, GC_PERIOD.toMillis(), GC_PERIOD.toMillis());
    }

    /**
     * @return The default instance of the connection pool.
     */
    public synchronized static DatabaseConnectionPoolV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseConnectionPoolV2(MYSQL_URL, DB_NAME, MYSQL_LOGIN, MYSQL_PASSWORD);
        }

        return INSTANCE;
    }

    /**
     * Creates a connection pool with the given properties.
     *
     * @param url The URL to the MySQL server.
     * @param database The database name.
     * @param username The username to login.
     * @param password The password of the username.
     */
    DatabaseConnectionPoolV2(@NotNull String url, @NotNull String database, @NotNull String username, @NotNull String password) {
        this.url = url;
        this.login = username;
        this.password = password;
        this.db = database;
    }

    /**
     * Creates a new connection to the database and adds it into the pool.
     *
     * @return A new connection to the database.
     * @throws RuntimeException if a new connection cannot be created.
     */
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

    /**
     * Finds an existing connection, or creates a new connection if no existing connections are available.
     *
     * @return A free connection from the pool, either by reusing one or creating one.
     * @throws SQLException if a database operation fails.
     */
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

    /**
     * Finds a connection from the pool matching a predicate.
     *
     * @param predicate Predicate to filter the connections in the pool.
     * @return A connection in the pool matching the given predicate, or {@code null} if none matches the predicate.
     */
    @Nullable
    private DatabaseConnectionV2 findConnection(@NotNull Predicate<DatabaseConnectionV2> predicate) {
        synchronized (pool) {
            return pool.stream()
                    .filter(predicate)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Executes a block of SQL statements on the SQL server managed by this pool.
     *
     * After the block finishes execution, the result is committed into the database.
     *
     * @param block The block of SQL statements to execute.
     * @param <R> The return type from the block.
     * @return The return value of the block. May be {@code null}.
     * @throws SQLException if the database operation fails.
     * @see DatabaseConnectionPoolV2#putTransactionWithId(long, ConnectionFunction)
     */
    @Nullable
    public <R> R execStmt(@NotNull ConnectionFunction<R> block) throws SQLException {
        final var connection = findOrNewConnection();

        return connection.execStmt(block);
    }

    /**
     * Obtains an ID to queue transactions on the SQL server managed by this pool.
     *
     * Note that this timeout has no relation with the transaction timeout set by the database. In fact, the timeout is
     * enforced by {@link DatabaseConnectionV2}, and therefore whether a transaction has timed out depends on both this
     * value and the transaction timeout set in the SQL server.
     *
     * @param timeout The timeout of this transaction.
     * @return A long value representing the newly created transaction.
     * @throws SQLException if the database operation fails.
     */
    public long getIdForTransaction(@NotNull Duration timeout) throws SQLException {
        final var connection = findOrNewConnection();
        return connection.getIdForTransaction(timeout);
    }

    /**
     * Executes a block of SQL statements on the SQL server with the given transaction ID.
     *
     * Note that calling this method does not commit the result. Instead,
     * {@link DatabaseConnectionPoolV2#executeTransaction(long, boolean)} must be called to commit or rollback the
     * transaction.
     *
     * @param id The ID of the transaction assigned by {@link DatabaseConnectionPoolV2#getIdForTransaction(Duration)}.
     * @param block The block of SQL statements to execute in the transaction.
     * @param <R> The return type from the block.
     * @return The return value of the block. May be {@code null}.
     * @throws SQLException if the database operation fails.
     * @see DatabaseConnectionPoolV2#execStmt(ConnectionFunction)
     */
    @Nullable
    public <R> R putTransactionWithId(long id, @NotNull ConnectionFunction<R> block) throws SQLException {
        final var connection = findConnection(it -> it.isInUse() && it.getTransactionId() == id);

        if (connection != null) {
            return connection.execTransaction(block);
        } else {
            return null;
        }
    }

    /**
     * Commits or rolls back a transaction on the SQL server.
     *
     * @param id The ID of the transaction assigned by {@link DatabaseConnectionPoolV2#getIdForTransaction(Duration)}.
     * @param shouldCommit If {@code true}, commits the transaction. Otherwise, rolls back the transaction.
     * @return {@code true} if the commit operation was successful. Failure to commit, rolling back, and no such
     * connection will all return {@code false}.
     */
    public boolean executeTransaction(long id, boolean shouldCommit) {
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

    /**
     * {@inheritDoc}
     *
     * Closes all connections managed by this connection pool, and clears the pool.
     */
    @Override
    public void close() {
        pool.forEach(con -> {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        pool.clear();
    }
}
