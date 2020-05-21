package comp4111.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooksDeleteDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksDeleteDataAccess.class);

    public static boolean deleteBook(long id) {
        boolean isSuccess;

        try {
            isSuccess = DatabaseConnectionPoolV2.getInstance().execStmt(connection -> {
                try (var stmt = connection.prepareStatement("DELETE FROM Book WHERE id = ?")) {
                    stmt.setLong(1, id);
                    return stmt.executeUpdate() > 0;
                }
            }).get();
        } catch (Exception e) {
            LOGGER.error("Unable to delete book", e);
            isSuccess = false;
        }

        return isSuccess;
    }
}
