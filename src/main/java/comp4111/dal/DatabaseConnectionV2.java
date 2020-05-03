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
import java.time.Instant;
import java.util.Objects;

public class DatabaseConnectionV2 implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionV2.class);
    static final long NULL_TRANSACTION_ID = -1;

    private final Connection connection;
    private boolean isClosed = false;

    @Nullable
    private Instant lastUsedTime = null;

    @Nullable
    private TransactionInfo txInfo = null;

    private static class TransactionInfo {

        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        public final int timeout;
        public final long txId;

        @NotNull
        public final Instant initTime = Instant.now();

        TransactionInfo(int timeout, boolean isOneTime) {
            this.timeout = timeout;
            this.txId = isOneTime ? NULL_TRANSACTION_ID : Math.abs(SECURE_RANDOM.nextLong());
        }
    }

    DatabaseConnectionV2(@NotNull String databaseUrl, @NotNull String user, @NotNull String password) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl, user, password);
        connection.setAutoCommit(false);
    }

    DatabaseConnectionV2(@NotNull String url, @NotNull String database, @NotNull String username, @NotNull String password) throws SQLException {
        this(String.format("%s/%s", url, database), username, password);
    }

    public synchronized Object execStmt(@NotNull ConnectionFunction block) throws SQLException {
        getIdForTransaction(0, true);
        final var obj = execTransaction(block);
        commit();
        return obj;
    }

    public synchronized long getIdForTransaction(int timeout) throws SQLException {
        return getIdForTransaction(timeout, false);
    }

    private synchronized long getIdForTransaction(int timeout, boolean isOneTime) throws SQLException {
        bindConnection(timeout, isOneTime);

        if (txInfo == null) {
            throw new IllegalStateException("Transaction ID should be valid");
        }
        return txInfo.txId;
    }

    public synchronized Object execTransaction(@NotNull ConnectionFunction block) throws SQLException {
        return block.accept(connection);
    }

    public synchronized boolean commit() {
        Objects.requireNonNull(txInfo, "Attempted to commit an unbound connection");

        final var timeSinceInit = Instant.now().toEpochMilli() - txInfo.initTime.toEpochMilli();

        boolean isCommitted;
        if (txInfo.timeout > 0 && timeSinceInit > txInfo.timeout) {
            try {
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

        unbindConnection();

        return isCommitted;
    }

    public synchronized void rollback() {
        Objects.requireNonNull(txInfo, "Attempted to rollback an unbound connection");

        try {
            connection.rollback();
        } catch (SQLException e) {
            LOGGER.error("Unable to rollback transaction", e);
        }
        unbindConnection();
    }

    private synchronized void bindConnection(int timeout, boolean isOneTime) throws SQLException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be a non-negative value");
        }
        if (!connection.isValid(0)) {
            throw new IllegalStateException("Attempted to open an already-closed connection");
        }
        if (txInfo != null) {
            throw new IllegalStateException("Attempted to bind a bound connection");
        }

        txInfo = new TransactionInfo(timeout, isOneTime);
        lastUsedTime = null;
    }

    private synchronized void unbindConnection() {
        if (txInfo == null) {
            throw new IllegalStateException("Attempted to unbind a unbound connection");
        }

        lastUsedTime = Instant.now();
        txInfo = null;
    }

    @NotNull
    public synchronized Connection getConnection() {
        if (txInfo == null) {
            throw new IllegalStateException("Cannot get connection of an unbound connection");
        }

        return connection;
    }

    synchronized long getTransactionId() {
        if (txInfo == null) {
            throw new IllegalStateException("Cannot get transaction ID of an unbound connection");
        }

        return txInfo.txId;
    }

    @Nullable
    synchronized Instant getLastUsedTime() {
        return lastUsedTime;
    }

    synchronized boolean isInUse() {
        return txInfo != null;
    }

    synchronized boolean isClosed() {
        return isClosed;
    }

    @Override
    public synchronized void close() throws Exception {
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
