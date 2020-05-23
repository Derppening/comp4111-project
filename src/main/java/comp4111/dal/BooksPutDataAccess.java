package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.function.ConnectionFunction;
import comp4111.util.QueryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BooksPutDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksPutDataAccess.class);

    /**
     * Updates the available state of the book with the given ID.
     *
     * @param con The database connection to use for the query. If {@code null}, use a new connection to execute the
     * query.
     * @param id The ID of the book to query.
     * @return {@code 0} for 200 response, {@code 1} for 400 response, {@code 2} for 404 response.
     */
    public static int updateBook(@Nullable Connection con, long id, boolean available) {
        final ConnectionFunction<Integer> block = connection -> getBook(connection, id, InnoDBLockMode.UPDATE)
                .thenApply(book -> {
                    if (book == null) {
                        return 2;
                    } else if (book.isAvailable() == available) {
                        return 1;
                    } else {
                        try (var stmt = connection.prepareStatement("UPDATE Book SET available = ? WHERE id = ?")) {
                            stmt.setBoolean(1, available);
                            stmt.setLong(2, id);
                            return stmt.executeUpdate() > 0 ? 0 : 1;
                        } catch (SQLException e) {
                            throw new CompletionException(e);
                        }
                    }
                })
                .join();

        CompletableFuture<Integer> result;
        try {
            if (con != null) {
                result = CompletableFuture.supplyAsync(() -> {
                    try {
                        return block.apply(con);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
            } else {
                result = DatabaseConnectionPoolV2.getInstance().execStmt(block);
            }
            return result.get();
        } catch (Exception e) {
            LOGGER.error("Caught error while updating book status", e);
            return 1;
        }
    }

    /**
     * Retrieves a book from the database with the given ID.
     *
     * @param con The database connection to use for the query. If {@code null}, use a new connection to execute the
     * query.
     * @param id The ID of the book to query.
     * @param lockMode The lock mode when executing the query.
     * @return The book obtained from the query.
     */
    @NotNull
    public static CompletableFuture<Book> getBook(
            @Nullable Connection con,
            long id,
            @NotNull InnoDBLockMode lockMode) {
        final var ext = String.format("where id = ? %s", lockMode.asSQLQueryComponent()).trim();

        final var params = new ArrayList<>();
        params.add(id);
        final var bookInDb = QueryUtils.queryTable(con, "Book", ext, params, Book::toBook);

        return bookInDb.thenApply(book -> book.isEmpty() ? null : book.get(0));
    }
}
