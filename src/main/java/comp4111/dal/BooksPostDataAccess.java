package comp4111.dal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BooksPostDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksPostDataAccess.class);

    private static class Book {

        private int id;
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
         * Creates a {@link Book} object from a database row.
         *
         * @param rs {@link ResultSet} from the query.
         * @return An object representing the record, with the ID.
         */
        @NotNull
        static Book from(@NotNull ResultSet rs) {
            try {
                assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

                final Book b = new Book(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getBoolean(6)
                );
                return b;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int addBook(String title, String author, String publisher, int year) {
        Book b = new Book(title, author, publisher, year);

        try (
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement stmt = con.prepareStatement("insert into Book values(null, ?, ?, ?, ?, ?)");
        ) {
            stmt.setString(1, b.title);
            stmt.setString(2, b.author);
            stmt.setString(3, b.publisher);
            stmt.setInt(4, b.year);
            stmt.setBoolean(5, b.available);
            stmt.execute();

            return getBook(b.title);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @return The ID of the book or {@code 0} if the book does not exist.
     */
    public static int getBook(String title) {
        try (Connection con = DatabaseConnection.getConnection()) {
            AtomicInteger result = new AtomicInteger();
            final var bookInDb = queryTableWithExtension(con, "Book", "where title = '" + title + "'", Book::from);
            bookInDb.forEach(b -> {
                // There should be only one result.
                result.set(b.id);
            });

            return result.intValue();
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return 0;
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
            final var rs = stmt.executeQuery("select * from " + tableName + " " + ext);
            while (rs.next()) {
                list.add(transform.apply(rs));
            }
        }
        return list;
    }
}
