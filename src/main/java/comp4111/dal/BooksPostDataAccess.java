package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.util.QueryUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BooksPostDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksPostDataAccess.class);

    public static long addBook(@NotNull String title, String author, String publisher, int year) {
        Book b = new Book(title, author, publisher, year);

        try (
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement("insert into Book values(null, ?, ?, ?, ?, ?)")
        ) {
            stmt.setString(1, b.getTitle());
            stmt.setString(2, b.getAuthor());
            stmt.setString(3, b.getPublisher());
            stmt.setInt(4, b.getYear());
            stmt.setBoolean(5, b.isAvailable());
            stmt.execute();

            return getBook(b.getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @return The ID of the book or {@code 0} if the book does not exist.
     */
    public static long getBook(String title) {
        try (Connection con = DatabaseConnection.getConnection()) {
            AtomicLong result = new AtomicLong();
            List<Object> params = new ArrayList<>();
            params.add(title);
            final var bookInDb = QueryUtils.queryTable(con, "Book", "where title = ?", params, Book::toBook);
            bookInDb.forEach(b -> {
                // There should be only one result.
                result.set(b.getId());
            });

            return result.longValue();
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return 0;
    }
}
