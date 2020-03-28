package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.BooksGetDataAccess;
import comp4111.handler.BooksGetHandler;
import comp4111.model.BooksGetResult;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BooksGetHandlerImpl extends BooksGetHandler {

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(request, response, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        BooksGetResult booksGetResult = BooksGetDataAccess.getBooks(getQueryId(), getQueryTitle(), getQueryAuthor(),
                getQueryLimit(), getQuerySort(), getQueryOrder());
        if (booksGetResult == null) {
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        } else if (booksGetResult.getFoundBooks() == 0) {
            response.setCode(HttpStatus.SC_NO_CONTENT);
        } else {
            // https://stackoverflow.com/a/13514884
            objectMapper.writeValue(outputStream, booksGetResult);
            final byte[] data = outputStream.toByteArray();

            response.setCode(HttpStatus.SC_OK);
            response.setEntity(new StringEntity(new String(data), ContentType.APPLICATION_JSON));
        }
    }
}
