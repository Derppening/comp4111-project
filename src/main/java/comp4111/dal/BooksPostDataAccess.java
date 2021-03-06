package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.util.QueryUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BooksPostDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksPostDataAccess.class);

    public static long addBook(@NotNull Book book) {
        // https://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
        long id;
        try {
            id = DatabaseConnectionPoolV2.getInstance().execStmt(connection -> {
                try (var stmt = connection.prepareStatement("INSERT IGNORE INTO Book VALUES(NULL, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, book.getTitle());
                    stmt.setString(2, book.getAuthor());
                    stmt.setString(3, book.getPublisher());
                    stmt.setInt(4, book.getYear());
                    stmt.setBoolean(5, book.isAvailable());
                    stmt.executeUpdate();

                    try (var generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            LOGGER.info("Inserted id={}", generatedKeys.getLong(1));
                            return generatedKeys.getLong(1);
                        } else {
                            return 0L;
                        }
                    }
                }
            }).get();
        } catch (Exception e) {
            LOGGER.error("Unable to insert book", e);
            id = 0;
        }

        return id;
    }

    @Deprecated
    public static long addBook(@NotNull String title, String author, String publisher, int year) {
        return addBook(new Book(title, author, publisher, year));
    }

    public static long addBook(@NotNull comp4111.model.Book book) {
        return addBook(new Book(book.getTitle(), book.getAuthor(), book.getPublisher(), book.getYear()));
    }

    /**
     * @return The ID of the book or {@code 0} if the book does not exist.
     */
    public static long getBook(String title) {
        try {
            AtomicLong result = new AtomicLong();
            List<Object> params = new ArrayList<>();
            params.add(title);
            final var bookInDb = QueryUtils.queryTable(null, "Book", "where title = ?", params, Book::toBook).get();
            bookInDb.forEach(b -> {
                // There should be only one result.
                result.set(b.getId());
            });

            return result.longValue();
        } catch (Exception e) {
            LOGGER.error("Error querying the table", e);
        }
        return 0;
    }
}
