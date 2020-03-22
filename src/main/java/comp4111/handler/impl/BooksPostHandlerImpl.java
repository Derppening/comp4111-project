package comp4111.handler.impl;

import comp4111.dal.BooksPostDataAccess;
import comp4111.handler.BooksPostHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksPostHandlerImpl extends BooksPostHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (BooksPostDataAccess.getBook(book.getTitle()) == 0) {
            // The book does not exist.
            int bookId = BooksPostDataAccess.addBook(book.getTitle(), book.getAuthor(), book.getPublisher(), book.getYear());
            response.setCode(HttpStatus.SC_CREATED);
            response.setHeader("Location", "/books/" + bookId);
        } else {
            // TODO(davidtang1006): (!)
            response.setCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }
    }
}
