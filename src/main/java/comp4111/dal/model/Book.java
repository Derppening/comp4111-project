package comp4111.dal.model;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Book {

    private long id;
    private String title;
    private String author;
    private String publisher;
    private int year;
    private boolean available;

    public Book() {
    }

    public Book(int id, @NotNull String title, @NotNull String author,
                @NotNull String publisher, int year, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.available = available;
    }

    public Book(@NotNull String title, @NotNull String author, @NotNull String publisher, int year) {
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
    protected static Book toBook(@NotNull ResultSet rs) {
        try {
            assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

            return new Book(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getBoolean(6)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link comp4111.model.Book} object from a database row.
     *
     * @param rs {@link ResultSet} from the query.
     * @return An object representing the record.
     */
    @NotNull
    protected static comp4111.model.Book toJsonBook(@NotNull ResultSet rs) {
        try {
            assert (!rs.isClosed() && !rs.isBeforeFirst() && !rs.isAfterLast());

            return new comp4111.model.Book(
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }
}
