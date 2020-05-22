package comp4111.dal;

import comp4111.model.TransactionPostRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    public static Long startNewTransaction() {
        try {
            return DatabaseConnectionPoolV2.getInstance().getIdForTransaction();
        } catch (Exception e) {
            LOGGER.error("Error starting a new transaction", e);
            return 0L;
        }
    }

    public static boolean commitOrCancelTransaction(Long transaction, @NotNull TransactionPostRequest.Operation operation) {
        final var shouldCommit = operation == TransactionPostRequest.Operation.COMMIT;
        final var result = DatabaseConnectionPoolV2.getInstance().executeTransaction(transaction, shouldCommit);

        return result == shouldCommit;
    }
}
