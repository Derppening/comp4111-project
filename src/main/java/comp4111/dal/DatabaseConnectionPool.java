package comp4111.dal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// https://www.baeldung.com/java-connection-pooling
public class DatabaseConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionPool.class);

    private List<Connection> connectionPool;
    private List<Connection> usedConnections = new ArrayList<>();
    private List<Timer> timers = new ArrayList<>();
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

    private class TimerTaskImpl extends TimerTask {

        private Connection con;

        private TimerTaskImpl(@NotNull Connection con) {
            this.con = con;
        }

        @Override
        public void run() {
            try {
                con.rollback();
                releaseConnection(con);
                LOGGER.info("A timeout transaction is cancelled");
            } catch (SQLException e) {
                LOGGER.error("Error rolling back", e);
            }
        }
    }

    public Connection getConnection() {
        if (connectionPool.size() != 0) {
            Connection connection = connectionPool
                    .remove(connectionPool.size() - 1);

            Timer timer = new Timer();
            timer.schedule(new TimerTaskImpl(connection), 90000);

            usedConnections.add(connection);
            timers.add(timer);
            return connection;
        }
        return null;
    }

    public void releaseConnection(@NotNull Connection connection) {
        int index = usedConnections.indexOf(connection);
        Timer timer = timers.get(index);
        timer.cancel();
        timers.remove(timer);

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
