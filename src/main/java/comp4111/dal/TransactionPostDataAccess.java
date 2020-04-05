package comp4111.dal;

import comp4111.model.TransactionPostRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static comp4111.dal.DatabaseConnection.connectionPool;

public class TransactionPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    public static int startNewTransaction() {
        try {
            Connection con = connectionPool.getConnection();
            if (con == null) {
                return 0;
            }

            return connectionPool.getUsedConnectionId(con);
        } catch (Exception e) {
            LOGGER.error("Error starting a new transaction", e);
        }
        return 0;
    }

    public static boolean commitOrCancelTransaction(int transaction, @NotNull TransactionPostRequest.Operation operation) {
        try {
            Connection con = connectionPool.getUsedConnection(transaction);
            if (con == null) {
                return false;
            }

            if (operation == TransactionPostRequest.Operation.COMMIT) {
                con.commit();
            } else {
                con.rollback();
            }
            connectionPool.releaseConnection(con);

            return true;
        } catch (SQLException e) {
            LOGGER.error("Error committing or cancelling the transaction", e);
        }
        return false;
    }
}
