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

    public DatabaseConnectionPool(String url, String user, String password) throws SQLException {
        connectionPool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection(url, user, password));
        }
    }

    private static Connection createConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
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

    public boolean releaseConnection(@NotNull Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    /**
     * @return {@code 0} for invalid {@link Connection}
     */
    public int getUsedConnectionId(Connection connection) {
        return usedConnections.indexOf(connection) + 1;
    }

    public Connection getUsedConnection(int id) {
        return usedConnections.get(id - 1);
    }
}
