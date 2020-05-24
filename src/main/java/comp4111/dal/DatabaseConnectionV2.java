package comp4111.dal;

import comp4111.function.ConnectionFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;

/**
 * An DAL over {@link Connection} to enable support for connection reuse.
 * <p>
 * Note:
 * <br>
 * Connection opening and closing refers to the act of opening/closing a connection to a SQL server, whereas connection
 * binding and unbinding refer to the act of assigning/un-assigning the connection to a specific transaction.
 */
public class DatabaseConnectionV2 implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionV2.class);

    /**
     * Transaction ID representing a transaction which is single-use only.
     *
     * Transactions of this ID is only used when executing single-use statements, such as via
     * {@link DatabaseConnectionV2#execStmt(ConnectionFunction)}.
     */
    static final long NULL_TRANSACTION_ID = -1;

    private final Connection connection;
    private boolean isClosed = false;

    /**
     * The default lock timeout as retrieved when the connection is first established to the database.
     */
    private final Duration defaultLockTimeout;

    /**
     * The information of the current transaction.
     */
    @Nullable
    private TransactionInfo txInfo = null;

    /**
     * A POD class for storing transaction information.
     */
    private static class TransactionInfo {

        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        /**
         * The timeout of this transaction, or {@code 0} if the transaction has no timeout.
         */
        public final Duration timeout;
        /**
         * The ID of this transaction, or {@link DatabaseConnectionV2#NULL_TRANSACTION_ID} if this transaction is a
         * one-time transaction managed internally by {@link DatabaseConnectionV2}.
         */
        public final long txId;

        /**
         * The time which this transaction is last operated on.
         */
        @NotNull
        public Instant lastOpTime;

        /**
         * @param timeout Timeout of this transaction. Set this value to {@link Duration#ZERO} for no timeout.
         * @param isOneTime If true, marks this transaction as one which does not need to expose its transaction ID
         * outside of {@link DatabaseConnectionV2}.
         */
        TransactionInfo(@NotNull Duration timeout, boolean isOneTime) {
            this.timeout = timeout;
            this.txId = isOneTime ? NULL_TRANSACTION_ID : Math.abs(SECURE_RANDOM.nextLong());
            this.lastOpTime = Instant.now();
        }

        /**
         * Marks this transaction as being used right now.
         *
         * When called, the method will first check whether this transaction is timed out. If not, the last operation
         * time will be updated accordingly as the beginning of the sliding window. Otherwise, the time will not be
         * updated to implicitly indicate that the transaction has timed out.
         */
        void markUsedNow() {
            final var now = Instant.now();
            if (!hasTimedOut(now)) {
                lastOpTime = Instant.now();
            }
        }

        /**
         * @return {@code true} if this transaction has timed out.
         */
        boolean hasTimedOut() {
            return hasTimedOut(Instant.now());
        }

        /**
         * @param time The {@link Instant} to check the time against.
         * @return {@code true} if this transaction has timed out against the given time.
         */
        private boolean hasTimedOut(@NotNull Instant time) {
            return time.toEpochMilli() - lastOpTime.toEpochMilli() > timeout.toMillis();
        }
    }

    /**
     * Creates a connection to a database server.
     *
     * @param databaseUrl The URL to the database.
     * @param user The user to login the database.
     * @param password The password of the user.
     * @throws SQLException if a database access error has occurred.
     */
    DatabaseConnectionV2(
            @NotNull String databaseUrl,
            @NotNull String user,
            @NotNull String password) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl, user, password);
        connection.setAutoCommit(false);
        defaultLockTimeout = DatabaseUtils.getLockTimeout(connection);
    }

    /**
     * Creates a connection to a database.
     *
     * @param url The URL to the database.
     * @param database The database to connection to.
     * @param username The user to login the database.
     * @param password The password of the user.
     * @throws SQLException if a database access error has occurred.
     */
    DatabaseConnectionV2(
            @NotNull String url,
            @NotNull String database,
            @NotNull String username,
            @NotNull String password) throws SQLException {
        this(String.format("%s/%s", url, database), username, password);
    }

    private synchronized void runBlocking(@NotNull ForkJoinPool.ManagedBlocker block) throws SQLException {
        try {
            ForkJoinPool.managedBlock(block);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Executes a block of SQL statements using this connection.
     *
     * @param block The block of SQL statements to execute.
     * @param <R> The return type from the block.
     * @return The return value of {@code block}.
     * @throws SQLException if a database access error has occurred.
     */
    public synchronized <R> R execStmt(@NotNull ConnectionFunction<R> block) throws SQLException {
        LOGGER.trace("execStmt(block=...)");

        final var blocker = new ForkJoinPool.ManagedBlocker() {

            R object;

            @Override
            public boolean block() {
                try {
                    DatabaseUtils.setLockTimeout(connection, defaultLockTimeout);
                    getIdForTransaction(Duration.ZERO, true);
                    object = execTransaction(block);
                    commit();
                } catch (Throwable tr) {
                    throw new RuntimeException(tr);
                }

                return true;
            }

            @Override
            public boolean isReleasable() {
                return false;
            }
        };

        runBlocking(blocker);
        return blocker.object;
    }

    /**
     * Obtains a transaction ID for a SQL transaction.
     *
     * @param txTimeout Timeout of the transaction.
     * @param lockTimeout Timeout for database locking.
     * @return The transaction ID.
     * @throws SQLException if a database access error has occurred.
     */
    public synchronized long getIdForTransaction(@NotNull Duration txTimeout, @Nullable Duration lockTimeout) throws SQLException {
        if (lockTimeout == null) {
            lockTimeout = this.defaultLockTimeout;
        }
        DatabaseUtils.setLockTimeout(connection, lockTimeout);

        return getIdForTransaction(txTimeout, false);
    }

    /**
     * Obtains a transaction ID for a SQL transaction.
     *
     * @param timeout Timeout of the transaction.
     * @param isOneTime If true, marks this transaction as one which does not need to expose its transaction ID.
     * outside of {@link DatabaseConnectionV2}.
     * @return The transaction ID.
     * @throws SQLException if a database access error has occurred.
     */
    private synchronized long getIdForTransaction(@NotNull Duration timeout, boolean isOneTime) throws SQLException {
        LOGGER.trace("getIdForTransaction(timeout={}, isOneTime={})", timeout, isOneTime);

        bindConnection(timeout, isOneTime);

        if (txInfo == null) {
            throw new IllegalStateException("Transaction ID should be valid");
        }
        return txInfo.txId;
    }

    /**
     * Adds a sequence of SQL statements to be committed to this transaction.
     *
     * @param block The block of SQL statements to execute.
     * @param <R> The return type from the block.
     * @return The return value of {@code block}.
     * @throws SQLException if a database access error has occurred.
     */
    public synchronized <R> R execTransaction(@NotNull ConnectionFunction<R> block) throws SQLException {
        LOGGER.trace("execTransaction(block=...)");

        final var blocker = new ForkJoinPool.ManagedBlocker() {

            R object;

            @Override
            public boolean block() {
                try {
                    object = block.apply(connection);
                } catch (Throwable tr) {
                    throw new RuntimeException(tr);
                }

                return true;
            }

            @Override
            public boolean isReleasable() {
                return false;
            }
        };

        if (txInfo == null) {
            throw new IllegalStateException("Attempted to execute a transaction on an unbound connection");
        }

        txInfo.markUsedNow();

        runBlocking(blocker);
        return blocker.object;
    }

    /**
     * Commits a transaction.
     *
     * This method will check whether the transaction has already timed out before committing the transaction. A timed
     * out transaction will always result in a rollback.
     *
     * If a commit operation fails, the transaction is rolled back.
     *
     * @return {@code true} if the operation succeeded.
     * @throws IllegalStateException if this connection currently does not serve a transaction.
     */
    public synchronized boolean commit() {
        LOGGER.trace("commit()");

        final var blocker = new ForkJoinPool.ManagedBlocker() {

            boolean isCommitted;

            @Override
            public boolean block() {
                if (txInfo == null) {
                    throw new IllegalStateException("Attempted to commit an unbound connection");
                }

                if (!txInfo.timeout.isZero() && txInfo.hasTimedOut()) {
                    try {
                        LOGGER.info("Transaction timed out: Rolling back transaction");
                        connection.rollback();
                    } catch (SQLException e) {
                        LOGGER.error("Unable to rollback expired transaction", e);
                    }
                    isCommitted = false;
                } else {
                    try {
                        connection.commit();
                        isCommitted = true;
                    } catch (SQLException e) {
                        LOGGER.error("Unable to commit transaction", e);
                        isCommitted = false;
                        try {
                            connection.rollback();
                        } catch (SQLException ee) {
                            LOGGER.error("Unable to rollback transaction", ee);
                        }
                    }
                }

                return true;
            }

            @Override
            public boolean isReleasable() {
                return false;
            }
        };

        try {
            runBlocking(blocker);
        } catch (SQLException ignored) {
        }
        unbindConnection();
        return blocker.isCommitted;
    }

    /**
     * Rolls back a transaction.
     *
     * @throws IllegalStateException if this connection currently does not serve a transaction.
     */
    public synchronized void rollback() {
        LOGGER.trace("rollback()");

        final var blocker = new ForkJoinPool.ManagedBlocker() {

            @Override
            public boolean block() {
                if (txInfo == null) {
                    throw new IllegalStateException("Attempted to commit an unbound connection");
                }

                try {
                    connection.rollback();
                } catch (SQLException e) {
                    LOGGER.error("Unable to rollback transaction", e);
                }

                return true;
            }

            @Override
            public boolean isReleasable() {
                return false;
            }
        };

        try {
            runBlocking(blocker);
        } catch (SQLException ignored) {
        }
        unbindConnection();
    }

    /**
     * Marks this connection as bound to a transaction.
     *
     * @param timeout Timeout of the transaction.
     * @param isOneTime If true, marks this transaction as one which does not need to expose its transaction ID.
     * @throws IllegalArgumentException if the timeout is a negative value.
     * @throws IllegalStateException    if the connection is not valid or a transaction is already bound to this
     *                                  connection.
     * @throws SQLException             if a database access error has occurred.
     */
    private synchronized void bindConnection(@NotNull Duration timeout, boolean isOneTime) throws SQLException {
        LOGGER.trace("bindConnection(timeout={}, isOneTime={})", timeout, isOneTime);
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be a non-negative value");
        }
        if (!connection.isValid(0)) {
            throw new IllegalStateException("Attempted to open an already-closed connection");
        }
        if (txInfo != null) {
            throw new IllegalStateException("Attempted to bind a bound connection");
        }

        txInfo = new TransactionInfo(timeout, isOneTime);
    }

    /**
     * Unbinds this connection from a transaction.
     *
     * @throws IllegalStateException if this connection is not bound to a transaction.
     */
    private synchronized void unbindConnection() {
        LOGGER.trace("unbindConnection()");
        if (txInfo == null) {
            throw new IllegalStateException("Attempted to unbind a unbound connection");
        }

        txInfo = null;
    }

    /**
     * @return The transaction ID bound to this connection.
     * @throws IllegalStateException if this connection is not bound to a transaction.
     */
    synchronized long getTransactionId() {
        if (txInfo == null) {
            throw new IllegalStateException("Cannot get transaction ID of an unbound connection");
        }

        return txInfo.txId;
    }

    /**
     * @return Whether this connection is currently used by a transaction.
     */
    synchronized boolean isInUse() {
        return txInfo != null;
    }

    /**
     * @return Whether this connection is closed, i.e. {@link DatabaseConnectionV2#close()} is invoked on this instance.
     */
    synchronized boolean isClosed() {
        return isClosed;
    }

    /**
     * {@inheritDoc}
     *
     * Closes the underlying connection managed by this instance.
     *
     * If a transaction is under way, the transaction will be rolled back before it is closed.
     *
     * @throws SQLException if a database access error has occurred.
     */
    @Override
    public synchronized void close() throws SQLException {
        LOGGER.trace("close()");
        if (isInUse()) {
            rollback();
        }
        connection.close();
        isClosed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseConnectionV2 that = (DatabaseConnectionV2) o;
        return connection.equals(that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection);
    }
}
