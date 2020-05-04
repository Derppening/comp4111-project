package comp4111.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.BooksGetDataAccess;
import comp4111.handler.BooksGetHandler;
import comp4111.model.BooksGetResult;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BooksGetHandlerImpl extends BooksGetHandler {

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        try {
            super.handle(requestObject, responseTrigger, context);
        } catch (IllegalArgumentException e) {
            return;
        }

        BooksGetResult booksGetResult = BooksGetDataAccess.getBooks(getQueryId(), getQueryTitle(), getQueryAuthor(),
                getQueryLimit(), getQuerySort(), getQueryOrder());
        if (booksGetResult == null) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_BAD_REQUEST).build();
            responseTrigger.submitResponse(response, context);
        } else if (booksGetResult.getFoundBooks() == 0) {
            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_NO_CONTENT).build();
            responseTrigger.submitResponse(response, context);
        } else {
            // https://stackoverflow.com/a/13514884
            objectMapper.writeValue(outputStream, booksGetResult);
            final byte[] data = outputStream.toByteArray();

            final AsyncResponseProducer response = AsyncResponseBuilder.create(HttpStatus.SC_OK)
                    .setEntity(new String(data), ContentType.APPLICATION_JSON).build();
            responseTrigger.submitResponse(response, context);
        }
    }
}
