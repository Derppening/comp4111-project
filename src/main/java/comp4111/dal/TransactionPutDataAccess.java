package comp4111.dal;

import comp4111.model.TransactionPutRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static comp4111.dal.DatabaseConnection.connectionPool;

public class TransactionPutDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    /**
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static int pushAction(int transaction, long bookId, @NotNull TransactionPutRequest.Action action) {
        try {
            Connection con = connectionPool.getUsedConnection(transaction);
            int transactionPutResult;
            if (action == TransactionPutRequest.Action.LOAN) {
                transactionPutResult = BooksPutDataAccess.updateBook(con, bookId, false);

                if (transactionPutResult != 0) {
                    con.rollback();
                    connectionPool.releaseConnection(con);
                }
            } else {
                transactionPutResult = BooksPutDataAccess.updateBook(con, bookId, true);

                if (transactionPutResult != 0) {
                    con.rollback();
                    connectionPool.releaseConnection(con);
                }
            }

            return transactionPutResult;
        } catch (SQLException e) {
            LOGGER.error("Error starting a new transaction", e);
        }
        return 1;
    }
}
