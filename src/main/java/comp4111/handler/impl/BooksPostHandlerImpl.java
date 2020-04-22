package comp4111.handler.impl;

import comp4111.dal.BooksPostDataAccess;
import comp4111.handler.BooksPostHandler;
import comp4111.util.HttpUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BooksPostHandlerImpl extends BooksPostHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        long bookId = BooksPostDataAccess.getBook(getBook().getTitle());
        if (bookId == 0) {
            // The book does not exist.
            long newBookId = BooksPostDataAccess.addBook(getBook().getTitle(), getBook().getAuthor(), getBook().getPublisher(), getBook().getYear());
            response.setCode(HttpStatus.SC_CREATED);
            response.setHeader("Location", "/books/" + newBookId);
            response.setEntity(new StringEntity(
                    String.format("%s%s/%d?token=%s",
                            HttpUtils.getServerHostnameFromRequest(request),
                            getHandlePattern(),
                            newBookId,
                            getToken()), StandardCharsets.UTF_8));
        } else {
            response.setCode(HttpStatus.SC_CONFLICT);
            response.setHeader("Duplicate record", "/books/" + bookId);
        }
    }
}
