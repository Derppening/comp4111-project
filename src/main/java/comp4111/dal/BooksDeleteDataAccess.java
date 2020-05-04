package comp4111.dal;

import java.sql.SQLException;

public class BooksDeleteDataAccess {

    public static boolean deleteBook(long id) {
        boolean isSuccess;

        try {
            isSuccess = DatabaseConnectionPoolV2.getInstance().execStmt(connection -> {
                try (var stmt = connection.prepareStatement("DELETE FROM Book WHERE id = ?")) {
                    stmt.setLong(1, id);
                    return stmt.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            isSuccess = false;
        }

        return isSuccess;
    }
}
