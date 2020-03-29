package comp4111.dal;

import comp4111.dal.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BooksDeleteDataAccess {

    public static boolean deleteBook(long id) {
        List<Book> book = BooksPutDataAccess.getBook(id);
        if (!book.isEmpty()) {
            try (
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement stmt = con.prepareStatement("delete from Book " +
                            "where id = ?")
            ) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
