package comp4111.dal;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// https://www.baeldung.com/java-connection-pooling
public class DatabaseConnectionPool {

    private List<Connection> connectionPool;
    private List<Connection> usedConnections = new ArrayList<>();
    private static final int INITIAL_POOL_SIZE = 10;

    public DatabaseConnectionPool(@NotNull String url, @NotNull String database, @NotNull String user, @NotNull String password) throws SQLException {
        connectionPool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection(url, database, user, password));
        }
    }

    private static Connection createConnection(String url, String database, String user, String password) throws SQLException {
        Connection con = DriverManager.getConnection(url + "/" + database, user, password);

        con.setAutoCommit(false);
        // https://dev.mysql.com/doc/refman/5.7/en/set-transaction.html
        // The default isolation level is repeatable read.
        // https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
        // Dirty reads are prevented. This is all we need.

        return con;
    }

    public Connection getConnection() {
        if (connectionPool.size() != 0) {
            Connection connection = connectionPool
                    .remove(connectionPool.size() - 1);
            usedConnections.add(connection);
            return connection;
        }
        return null;
    }

    public void releaseConnection(@NotNull Connection connection) {
        connectionPool.add(connection);
        usedConnections.remove(connection);
    }

    /**
     * @return {@code 0} for invalid {@link Connection}
     */
    public int getUsedConnectionId(Connection connection) {
        return usedConnections.indexOf(connection) + 1;
    }

    public Connection getUsedConnection(int id) {
        try {
            return usedConnections.get(id - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
