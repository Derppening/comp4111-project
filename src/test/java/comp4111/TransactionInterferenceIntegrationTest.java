package comp4111;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.DatabaseConnection;
import comp4111.handler.*;
import comp4111.model.Book;
import comp4111.model.LoginRequest;
import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPutRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TransactionInterferenceIntegrationTest extends AbstractServerTest {

    private ObjectMapper objectMapper;
    private String token;
    private Long bookId2;
    private Long transactionId1;
    private Long transactionId2;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        assumeTrue(() -> {
            try (@SuppressWarnings("unused") var con = DriverManager.getConnection(DatabaseConnection.MYSQL_URL, DatabaseConnection.MYSQL_LOGIN, DatabaseConnection.MYSQL_PASSWORD)) {
                return true;
            } catch (Throwable tr) {
                return false;
            }
        }, "Database not started; Skipping live integration tests");

        DatabaseConnection.setConfig();
        MainApplication.createDefaultUsers();

        {
            HttpPathHandler[] handlers = new HttpPathHandler[MainApplication.PATTERN_HANDLER.size()];
            MainApplication.PATTERN_HANDLER.values().toArray(handlers);
            registerAndStartServer(handlers);
        }

        objectMapper = JacksonUtils.getDefaultObjectMapper();
    }

    void logInFirst() throws Exception {
        final var loginRequest = new LoginRequest("user001", "pass001");
        @Language("JSON") final var payload = objectMapper.writeValueAsString(loginRequest);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, LoginHandler.HANDLE_PATTERN, entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var token = objectMapper.readTree(responseJson).get("Token").textValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.token = token;
        }
    }

    void populateBooks() throws Exception {
        final var book = new Book(
                "Alice",
                "Lewis",
                "Mac",
                2020
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assumeTrue(HttpStatus.SC_CREATED == response.getCode());

            final var pattern = Pattern.compile("/books/(.+)");
            final var matcher = pattern.matcher(response.getHeader("Location").getValue());
            assumeTrue(matcher.matches());
            bookId2 = Long.parseLong(matcher.group(1));
        }
    }

    void requestATransactionId_1() throws Exception {
        final var entity = new StringEntity("");

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var transactionId = objectMapper.readTree(responseJson).get("Transaction").longValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.transactionId1 = transactionId;
        }
    }

    void requestATransactionId_2() throws Exception {
        final var entity = new StringEntity("");

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var transactionId = objectMapper.readTree(responseJson).get("Transaction").longValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.transactionId2 = transactionId;
        }
    }

    void pushAValidActionIntoATransaction_1() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId1, bookId2, TransactionPutRequest.Action.LOAN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void pushAnInvalidActionIntoATransaction_2() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId2, bookId2, TransactionPutRequest.Action.LOAN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void commitTheTransaction_1() throws Exception {
        final var transaction = new TransactionPostRequest(transactionId1, TransactionPostRequest.Operation.COMMIT);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void commitTheTransaction_2() throws Exception {
        final var transaction = new TransactionPostRequest(transactionId2, TransactionPostRequest.Operation.COMMIT);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void returnBook2() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": true}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId2, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void logOutWithTheCorrectToken() throws Exception {
        try (var response = makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    @Test
    void mainTest() throws Exception {
        logInFirst();
        populateBooks();
        requestATransactionId_1();
        requestATransactionId_2();
        pushAValidActionIntoATransaction_1();
        pushAnInvalidActionIntoATransaction_2();
        commitTheTransaction_1();
        commitTheTransaction_2();
        returnBook2();
        logOutWithTheCorrectToken();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null);
        }
        super.tearDown();

        DatabaseUtils.dropDatabase(DatabaseConnection.DB_NAME);
        DatabaseConnection.cleanUp();

        token = null;
        objectMapper = null;
    }
}
