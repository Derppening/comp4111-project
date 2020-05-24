package comp4111.dal;

import comp4111.model.TransactionPutRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class TransactionPutDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    /**
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static int pushAction(@NotNull Long transaction, long bookId, @NotNull TransactionPutRequest.Action action) {
        try {
            final var res = DatabaseConnectionPoolV2.getInstance().putTransactionWithId(transaction, connection -> {
                final var savepoint = connection.setSavepoint();

                int transactionPutResult;
                if (action == TransactionPutRequest.Action.LOAN) {
                    transactionPutResult = BooksPutDataAccess.updateBook(connection, bookId, false);
                } else {
                    transactionPutResult = BooksPutDataAccess.updateBook(connection, bookId, true);
                }

                if (transactionPutResult != 0) {
                    connection.rollback(savepoint);
                }

                return transactionPutResult;
            });

            return res != null ? res : 1;
        } catch (SQLException e) {
            LOGGER.error("Error starting a new transaction", e);
            return 1;
        }
    }
}
