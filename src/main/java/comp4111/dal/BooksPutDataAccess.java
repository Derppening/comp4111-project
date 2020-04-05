package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.util.QueryUtils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BooksPutDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksPutDataAccess.class);

    /**
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static int updateBook(long id, boolean available) {
        List<Book> book = getBook(id);
        if (book.isEmpty()) {
            return 2;
        } else if (book.get(0).isAvailable() == available) {
            return 1;
        } else {
            try (
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement stmt = con.prepareStatement("update Book " +
                            "set available = ? " +
                            "where id = ?")
            ) {
                stmt.setBoolean(1, available);
                stmt.setLong(2, id);
                return stmt.executeUpdate() > 0 ? 0 : 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    /**
     * This is the same as {@link BooksPutDataAccess#updateBook(long id, boolean available)} except that the connection is specified.
     *
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static int updateBook(@NotNull Connection con, long id, boolean available) {
        List<Book> book = getBook(con, id);
        if (book.isEmpty()) {
            return 2;
        } else if (book.get(0).isAvailable() == available) {
            return 1;
        } else {
            try (
                    PreparedStatement stmt = con.prepareStatement("update Book " +
                            "set available = ? " +
                            "where id = ?")
            ) {
                stmt.setBoolean(1, available);
                stmt.setLong(2, id);
                return stmt.executeUpdate() > 0 ? 0 : 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static List<Book> getBook(Long id) {
        List<Book> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            List<Object> params = new ArrayList<>();
            params.add(id);
            final var bookInDb = QueryUtils.queryTable(con, "Book", "where id = ?", params, Book::toBook);
            result.addAll(bookInDb); // There should be only one result.

            return result;
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return result;
    }

    /**
     * This is the same as {@link BooksPutDataAccess#getBook(Long id)} except that the connection is specified.
     *
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static List<Book> getBook(@NotNull Connection con, Long id) {
        List<Book> result = new ArrayList<>();
        try {
            List<Object> params = new ArrayList<>();
            params.add(id);
            final var bookInDb = QueryUtils.queryTable(con, "Book", "where id = ?", params, Book::toBook);
            result.addAll(bookInDb); // There should be only one result.

            return result;
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return result;
    }
}
