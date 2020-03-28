package comp4111.dal;

import comp4111.model.BooksGetResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BooksGetDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksGetDataAccess.class);

    private static class Book {

        private long id;
        private String title;
        private String author;
        private String publisher;
        private int year;
        private boolean available;

        private Book(int id, @NotNull String title, @NotNull String author,
                     @NotNull String publisher, int year, boolean available) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.year = year;
            this.available = available;
        }

        private Book(@NotNull String title, @NotNull String author, @NotNull String publisher, int year) {
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.year = year;
            this.available = true;
        }

        /**
         * Creates a {@link comp4111.model.Book} object from a database row.
         *
         * @param rs {@link ResultSet} from the query.
         * @return An object representing the record.
         */
        @NotNull
        static comp4111.model.Book toJsonBook(@NotNull ResultSet rs) {
            try {
                assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

                final comp4111.model.Book b = new comp4111.model.Book(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5)
                );
                return b;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static BooksGetResult getBooks(Long queryId, String queryTitle, String queryAuthor,
                                          Integer queryLimit, String querySort, String queryOrder) {
        List<String> list = new ArrayList<>();
        if (queryId != null) {
            list.add("id = " + queryId);
        }
        if (queryTitle != null) {
            // https://stackoverflow.com/a/2876802
            list.add("lower(title) like '%" + queryTitle + "%'");
        }
        if (queryAuthor != null) {
            list.add("lower(author) like '%" + queryAuthor + "%'");
        }
        String chunk1 = list.isEmpty() ? "" : "where " + String.join(" or ", list);

        String chunk2;
        if (!(querySort == null && queryOrder != null)) {
            if (querySort == null) {
                // queryOrder is also null.
                chunk2 = "";
            } else if (queryOrder == null) {
                if (querySort.equals("id") || querySort.equals("title") || querySort.equals("author")) {
                    chunk2 = "order by " + querySort;
                } else {
                    // The query is invalid.
                    return null;
                }
            } else {
                if ((querySort.equals("id") || querySort.equals("title") || querySort.equals("author")) &&
                        (queryOrder.equals("asc") || queryOrder.equals("desc"))) {
                    chunk2 = "order by " + querySort + " " + queryOrder;
                } else {
                    // The query is invalid.
                    return null;
                }
            }
        } else {
            // The query is invalid.
            return null;
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            final var booksInDb = queryTableWithExtension(con, "Book",
                    String.join(" ", chunk1, chunk2).trim(),
                    Book::toJsonBook
            );
            if (queryLimit == null) {
                return new BooksGetResult(booksInDb);
            } else {
                return new BooksGetResult(booksInDb.subList(0, queryLimit.intValue()));
            }
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return null;
    }

    /**
     * Queries a table with extra clauses, converting them into Java objects.
     *
     * @param con {@link Connection} to the database.
     * @param tableName Name of the table to query.
     * @param transform Transformation function to convert a {@link ResultSet} row into a Java object.
     * @param <T> Type of the object in Java.
     * @return {@link List} of rows, converted into Java objects.
     */
    private static <T> List<T> queryTableWithExtension(@NotNull final Connection con, @NotNull String tableName,
                                                       @NotNull String ext, @NotNull Function<ResultSet, T> transform) throws SQLException {
        final var list = new ArrayList<T>();
        try (Statement stmt = con.createStatement()) {
            // https://www.w3schools.com/sql/sql_select.asp
            final var rs = ext.equals("") ? stmt.executeQuery("select * from " + tableName) :
                    stmt.executeQuery("select * from " + tableName + " " + ext);
            while (rs.next()) {
                list.add(transform.apply(rs));
            }
        }
        return list;
    }
}
