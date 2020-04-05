package comp4111.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static comp4111.dal.DatabaseConnection.connectionPool;

public class TransactionPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    public static int startNewTransaction() {
        try {
            Connection con = connectionPool.getConnection();
            if (con == null) {
                return 0;
            }

            con.setAutoCommit(false);
            return connectionPool.getUsedConnectionId(con);
        } catch (Exception e) {
            LOGGER.error("Error starting a new transaction", e);
        }
        return 0;
    }
}
