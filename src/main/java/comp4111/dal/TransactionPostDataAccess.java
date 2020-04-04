package comp4111.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;

import static comp4111.dal.DatabaseConnection.connectionPool;

public class TransactionPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPostDataAccess.class);

    public static int startNewTransaction() {
        try (
                Connection con = connectionPool.getConnection();
                Statement stmt = con.createStatement()
        ) {
            return connectionPool.getUsedConnectionId(con);
        } catch (Exception e) {
            LOGGER.error("Error starting a new transaction", e);
        }
        return 0;
    }
}
