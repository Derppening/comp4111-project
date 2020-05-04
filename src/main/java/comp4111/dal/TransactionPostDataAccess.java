package comp4111.dal;

import comp4111.model.TransactionPostRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class TransactionPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    public static Long startNewTransaction() {
        try {
            return DatabaseConnectionPoolV2.getInstance().getIdForTransaction(90_000);
        } catch (Exception e) {
            LOGGER.error("Error starting a new transaction", e);
            return 0L;
        }
    }

    public static boolean commitOrCancelTransaction(Long transaction, @NotNull TransactionPostRequest.Operation operation) {
        try {
            return DatabaseConnectionPoolV2.getInstance().executeTransaction(transaction, operation == TransactionPostRequest.Operation.COMMIT);
        } catch (SQLException e) {
            LOGGER.error("Error committing or cancelling the transaction", e);
            return false;
        }
    }
}
