package comp4111.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.BooksGetDataAccess;
import comp4111.exception.HttpHandlingException;
import comp4111.handler.BooksGetHandler;
import comp4111.handler.HttpAsyncEndpointHandler;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.concurrent.CompletionException;

public class BooksGetHandlerImpl extends BooksGetHandler {

    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    @Override
    public void handle(Message<HttpRequest, String> requestObject, ResponseTrigger responseTrigger, HttpContext context) {
        super.handleAsync(requestObject)
                .thenApplyAsync(BooksGetDataAccess::getBooksAsync)
                .thenApplyAsync(result -> {
                    if (result.getFoundBooks() == 0) {
                        throw new CompletionException("No books found", new HttpHandlingException(HttpStatus.SC_NO_CONTENT));
                    }
                    return result;
                })
                .thenApplyAsync(result -> {
                    try {
                        return objectMapper.writeValueAsString(result);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(new HttpHandlingException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e));
                    }
                })
                .thenApplyAsync(json -> AsyncResponseBuilder.create(HttpStatus.SC_OK).setEntity(json, ContentType.APPLICATION_JSON).build())
                .exceptionally(this::exceptionToResponse)
                .thenAcceptAsync(response -> HttpAsyncEndpointHandler.emitResponse(response, responseTrigger, context));
    }
}
