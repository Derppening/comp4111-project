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
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    static final long NULL_TRANSACTION_ID = -1;

    private final Connection connection;
    private int timeout = -1;
    @Nullable
    private Instant opInitTime = null;
    @Nullable
    private Instant lastUsedTime = null;
    private long txId = NULL_TRANSACTION_ID;
    private boolean isClosed = false;

    DatabaseConnectionV2(@NotNull String databaseUrl, @NotNull String user, @NotNull String password) throws SQLException {
        connection = DriverManager.getConnection(databaseUrl, user, password);
    }

    DatabaseConnectionV2(@NotNull String url, @NotNull String database, @NotNull String username, @NotNull String password) throws SQLException {
        this(String.format("%s/%s", url, database), username, password);
    }

    public Object execStmt(@NotNull ConnectionFunction block) throws SQLException {
        getIdForTransaction(0);
        final var obj = block.accept(connection);
        commit();
        return obj;
    }

    public long getIdForTransaction(int timeout) throws SQLException {
        bindConnection(timeout);

        if (txId == NULL_TRANSACTION_ID) {
            throw new IllegalStateException("Transaction ID should be valid");
        }
        return txId;
    }

    public boolean commit() throws SQLException {
        if (connection.getAutoCommit()) {
            throw new IllegalStateException("Attempted to commit an auto-commit transaction");
        }

        Objects.requireNonNull(opInitTime, "Attempted to commit an idle connection");

        final var timeSinceInit = Instant.now().toEpochMilli() - opInitTime.toEpochMilli();

        final boolean isCommitted;
        if (timeout > 0 && timeSinceInit > timeout) {
            connection.rollback();
            isCommitted = false;
        } else {
            connection.commit();
            isCommitted = true;
        }

        unbindConnection();

        return isCommitted;
    }

    public void rollback() throws SQLException {
        if (connection.getAutoCommit()) {
            throw new IllegalStateException("Attempted to rollback an auto-commit transaction");
        }

        Objects.requireNonNull(opInitTime, "Attempted to rollback an idle connection");

        connection.rollback();
        unbindConnection();
    }

    private void bindConnection(int timeout) throws SQLException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be a non-negative value");
        }
        if (!connection.isValid(0)) {
            throw new IllegalStateException("Attempted to open an already-closed connection");
        }
        if (opInitTime != null) {
            throw new IllegalStateException("Attempted to re-use an in-use connection");
        }

        this.timeout = timeout;
        opInitTime = Instant.now();
        lastUsedTime = null;
        txId = Math.abs(SECURE_RANDOM.nextLong());
        connection.setAutoCommit(false);
    }

    private void unbindConnection() {
        opInitTime = null;
        lastUsedTime = Instant.now();
        txId = NULL_TRANSACTION_ID;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    @Nullable
    Long getTransactionId() {
        return txId != NULL_TRANSACTION_ID ? txId : null;
    }

    @Nullable
    Instant getLastUsedTime() {
        return lastUsedTime;
    }

    boolean isInUse() {
        return lastUsedTime == null;
    }

    boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws Exception {
        if (isInUse()) {
            commit();
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
