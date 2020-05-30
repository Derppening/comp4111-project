package comp4111.dal;

import comp4111.dal.model.Book;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.BooksGetHandler;
import comp4111.model.BooksGetResult;
import comp4111.util.QueryUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

public class BooksGetDataAccess extends Book {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooksGetDataAccess.class);

    @Nullable
    public static BooksGetResult getBooksAsync(@NotNull BooksGetHandler.QueryParams queryParams) {
        final var params = new ArrayList<>();
        final var list = new ArrayList<String>();
        if (queryParams.id != null) {
            list.add("id = ?");
            params.add(queryParams.id);
        }
        if (queryParams.title != null) {
            // https://stackoverflow.com/a/2876802
            list.add("LOWER(title) LIKE ?");
            params.add("%" + queryParams.title + "%");
        }
        if (queryParams.author != null) {
            list.add("LOWER(author) LIKE ?");
            params.add("%" + queryParams.author + "%");
        }
        final var chunk1 = list.isEmpty() ? "" : "WHERE " + String.join(" OR ", list);

        if (queryParams.sort == BooksGetHandler.QueryParams.SortField.NONE && queryParams.order != BooksGetHandler.QueryParams.OutputOrder.NONE) {
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST));
        }

        final String chunk2;
        if (queryParams.sort == BooksGetHandler.QueryParams.SortField.NONE) {
            chunk2 = "";
        } else if (queryParams.order == BooksGetHandler.QueryParams.OutputOrder.NONE) {
            chunk2 = "ORDER BY " + queryParams.sort.toSQLComponent();
        } else {
            chunk2 = "ORDER BY " + queryParams.sort.toSQLComponent() + " " + queryParams.order.toSQLComponent();
        }

        try {
            final var ext = String.join(" ", chunk1, chunk2, InnoDBLockMode.SHARE.asSQLQueryComponent()).trim();

            final var booksInDb = QueryUtils.queryTable(
                    null,
                    "Book",
                    ext,
                    params,
                    Book::toJsonBook)
                    .get();

            if (queryParams.limit == null || booksInDb.size() <= queryParams.limit) {
                return new BooksGetResult(booksInDb);
            } else {
                return new BooksGetResult(booksInDb.subList(0, queryParams.limit));
            }
        } catch (Exception e) {
            LOGGER.error("Error querying the table", e);
            throw new CompletionException(new HttpHandlingException(HttpStatus.SC_BAD_REQUEST, e));
        }
    }

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

        try {
            final var ext = String.join(" ", chunk1, chunk2, InnoDBLockMode.SHARE.asSQLQueryComponent()).trim();

            final var booksInDb = QueryUtils.queryTable(
                    null,
                    "Book",
                    ext,
                    params,
                    Book::toJsonBook)
                    .get();

            if (queryLimit == null || booksInDb.size() <= queryLimit) {
                return new BooksGetResult(booksInDb);
            } else {
                return new BooksGetResult(booksInDb.subList(0, queryLimit));
            }
        } catch (Exception e) {
            LOGGER.error("Error querying the table", e);
            return null;
        }
    }
}
