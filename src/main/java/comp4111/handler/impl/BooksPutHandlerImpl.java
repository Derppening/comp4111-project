package comp4111.handler.impl;

import comp4111.dal.BooksPutDataAccess;
import comp4111.handler.BooksPutHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class BooksPutHandlerImpl extends BooksPutHandler {

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        int booksPutResult = BooksPutDataAccess.updateBook(getBookId(), getAvailable());
        if (booksPutResult == 0) {
            response.setCode(HttpStatus.SC_OK);
        } else if (booksPutResult == 1) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            response.setCode(HttpStatus.SC_NOT_FOUND);
            response.setReasonPhrase("No book record");
        }
    }
}
