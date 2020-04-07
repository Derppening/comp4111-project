package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.model.BooksGetResult;
import comp4111.util.QueryUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BooksGetDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksGetDataAccess.class);

    public static BooksGetResult getBooks(@Nullable Long queryId,
                                          @Nullable String queryTitle,
                                          @Nullable String queryAuthor,
                                          @Nullable Integer queryLimit,
                                          @Nullable String querySort,
                                          @Nullable String queryOrder) {
        List<Object> params = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (queryId != null) {
            list.add("id = ?");
            params.add(queryId);
        }
        if (queryTitle != null) {
            // https://stackoverflow.com/a/2876802
            list.add("lower(title) like ?");
            params.add("%" + queryTitle + "%");
        }
        if (queryAuthor != null) {
            list.add("lower(author) like ?");
            params.add("%" + queryAuthor + "%");
        }
        String chunk1 = list.isEmpty() ? "" : "where " + String.join(" or ", list);

        String chunk2;
        if (!(querySort == null && queryOrder != null)) {
            if (querySort == null) {
                // queryOrder is also null.
                chunk2 = "";
            } else if (queryOrder == null) {
                if (querySort.equals("id") || querySort.equals("title") || querySort.equals("author") || querySort.equals("year")) {
                    chunk2 = "order by " + querySort;
                } else {
                    // The query is invalid.
                    return null;
                }
            } else {
                if ((querySort.equals("id") || querySort.equals("title") || querySort.equals("author") || querySort.equals("year")) &&
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
            final var booksInDb = QueryUtils.queryTable(con, "Book",
                    String.join(" ", chunk1, chunk2).trim(), params,
                    Book::toJsonBook
            );
            if (queryLimit == null || booksInDb.size() <= queryLimit) {
                return new BooksGetResult(booksInDb);
            } else {
                return new BooksGetResult(booksInDb.subList(0, queryLimit));
            }
        } catch (SQLException e) {
            LOGGER.error("Error querying the table", e);
        }
        return null;
    }
}
