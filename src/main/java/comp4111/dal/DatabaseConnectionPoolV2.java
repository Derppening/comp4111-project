package comp4111.dal;

import comp4111.function.ConnectionFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

import static comp4111.dal.DatabaseInfo.*;

/**
 * A connection pool for grouping connections into one SQL server.
 *
 * All SQL connection properties are read from {@code mysql.properties} in the resources directory.
 */
public class DatabaseConnectionPoolV2 implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionPoolV2.class);

    /**
     * The default timeout of a transaction.
     */
    private static final Duration DEFAULT_TX_TIMEOUT = Duration.ofSeconds(90);

    private static DatabaseConnectionPoolV2 INSTANCE = null;

    private final String url;
    private final String login;
    private final String password;
    private final String db;

    /**
     * The default timeout the database waits when a database or row(s) is locked. If the value is {@code null}, the
     * global default value of the database will be used.
     */
    @Nullable
    private Duration defaultLockTimeout;
    /**
     * The default timeout of any transaction.
     */
    @NotNull
    private Duration defaultTxTimeout = DEFAULT_TX_TIMEOUT;

    /**
     * The pool of connection instances.
     */
    private final Set<DatabaseConnectionV2> connectionPool = new HashSet<>();

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
    DatabaseConnectionPoolV2(
            @NotNull String url,
            @NotNull String database,
            @NotNull String username,
            @NotNull String password) {
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
    private CompletableFuture<DatabaseConnectionV2> newConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final var connection = new DatabaseConnectionV2(url, db, login, password);
                synchronized (connectionPool) {
                    connectionPool.add(connection);
                }
                return connection;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Finds an existing connection, or creates a new connection if no existing connections are available.
     *
     * @return A free connection from the pool, either by reusing one or creating one.
     */
    @NotNull
    private CompletableFuture<DatabaseConnectionV2> findOrNewConnection() {
        synchronized (connectionPool) {
            return connectionPool.stream()
                    .filter(con -> !con.isInUse())
                    .findAny()
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(this::newConnection);
        }
    }

    /**
     * Finds a connection from the pool matching a predicate.
     *
     * @param predicate Predicate to filter the connections in the pool.
     * @return A connection in the pool matching the given predicate, or {@code null} if none matches the predicate.
     */
    @NotNull
    private CompletableFuture<@Nullable DatabaseConnectionV2> findConnection(@NotNull Predicate<DatabaseConnectionV2> predicate) {
        synchronized (connectionPool) {
            return CompletableFuture.supplyAsync(() -> connectionPool.stream()
                    .filter(predicate)
                    .findFirst()
                    .orElse(null));
        }
    }

    /**
     * Evicts a connection from the connection pool if it is closed.
     *
     * @param connection Connection to check.
     */
    private void evictConnectionIfClosed(@NotNull DatabaseConnectionV2 connection) {
        synchronized (connectionPool) {
            if (connection.isClosed()) {
                connectionPool.remove(connection);
            }
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
     * @see DatabaseConnectionPoolV2#putTransactionWithId(long, ConnectionFunction)
     */
    @NotNull
    public <R> CompletableFuture<R> execStmt(@NotNull ConnectionFunction<R> block) {
        return findOrNewConnection()
                .thenApplyAsync(connection -> {
                    try {
                        final var result = connection.execStmt(block);
                        evictConnectionIfClosed(connection);
                        return result;
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    /**
     * Obtains an ID to queue transactions on the SQL server managed by this pool.
     *
     * Note that this timeout has no relation with the transaction timeout set by the database. In fact, the timeout is
     * enforced by {@link DatabaseConnectionV2}, and therefore whether a transaction has timed out depends on both this
     * value and the transaction timeout set in the SQL server.
     *
     * @return A long value representing the newly created transaction.
     */
    @NotNull
    public CompletableFuture<Long> getIdForTransaction() {
        return findOrNewConnection()
                .thenApplyAsync(connection -> {
                    try {
                        return connection.getIdForTransaction(defaultTxTimeout, defaultLockTimeout);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    /**
     * Executes a block of SQL statements on the SQL server with the given transaction ID.
     *
     * Note that calling this method does not commit the result. Instead,
     * {@link DatabaseConnectionPoolV2#executeTransaction(long, boolean)} must be called to commit or rollback the
     * transaction.
     *
     * @param id The ID of the transaction assigned by {@link DatabaseConnectionPoolV2#getIdForTransaction()}.
     * @param block The block of SQL statements to execute in the transaction.
     * @param <R> The return type from the block.
     * @return The return value of the block. May be {@code null}.
     * @see DatabaseConnectionPoolV2#execStmt(ConnectionFunction)
     */
    @NotNull
    public <R> CompletableFuture<R> putTransactionWithId(long id, @NotNull ConnectionFunction<R> block) {
        return findConnection(it -> it.isInUse() && it.getTransactionId() == id)
                .thenApplyAsync(connection -> {
                    if (connection != null) {
                        try {
                            return connection.execTransaction(block);
                        } catch (SQLException e) {
                            throw new CompletionException(e);
                        }
                    } else {
                        return null;
                    }
                });
    }

    /**
     * Commits or rolls back a transaction on the SQL server.
     *
     * @param id The ID of the transaction assigned by {@link DatabaseConnectionPoolV2#getIdForTransaction()}.
     * @param shouldCommit If {@code true}, commits the transaction. Otherwise, rolls back the transaction.
     * @return {@code true} if the commit operation was successful. Failure to commit, rolling back, and no such
     * connection will all return {@code false}.
     */
    @NotNull
    public CompletableFuture<Boolean> executeTransaction(long id, boolean shouldCommit) {
        return findConnection(it -> it.isInUse() && it.getTransactionId() == id)
                .thenApplyAsync(connection -> {
                    final boolean committed;
                    if (connection != null) {
                        if (shouldCommit) {
                            committed = connection.commit();
                        } else {
                            connection.rollback();
                            committed = false;
                        }

                        evictConnectionIfClosed(connection);
                    } else {
                        committed = false;
                    }
                    return committed;
                });
    }

    /**
     * Sets the default lock timeout for all transactions initiated by this pool.
     *
     * @param timeout New lock timeout.
     */
    public synchronized void setDefaultLockTimeout(@NotNull Duration timeout) {
        this.defaultLockTimeout = timeout;
    }

    /**
     * Resets the default lock timeout for all transactions initiated by this pool.
     */
    public synchronized void resetDefaultLockTimeout() {
        this.defaultLockTimeout = null;
    }

    /**
     * Sets the default transaction timeout for all transactions initiated by this pool.
     *
     * @param timeout New transaction timeout.
     */
    public synchronized void setDefaultTxTimeout(@NotNull Duration timeout) {
        this.defaultTxTimeout = timeout;
    }

    /**
     * Resets the default transaction timeout for all transactions initiated by this pool.
     *
     * The default value is {@link DatabaseConnectionPoolV2#DEFAULT_TX_TIMEOUT}.
     */
    public synchronized void resetDefaultTxTimeout() {
        this.defaultTxTimeout = DEFAULT_TX_TIMEOUT;
    }

    /**
     * {@inheritDoc}
     *
     * Closes all connections managed by this connection pool, and clears the pool. The lock timeout and transaction
     * timeout defaults will also be reverted to their original values.
     */
    @Override
    public void close() {
        synchronized (connectionPool) {
            connectionPool.forEach(con -> {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            connectionPool.clear();
        }

        resetDefaultLockTimeout();
        resetDefaultTxTimeout();
    }
}
