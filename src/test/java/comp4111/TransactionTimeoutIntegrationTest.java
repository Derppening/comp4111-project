package comp4111;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.DatabaseConnectionPoolV2;
import comp4111.dal.DatabaseUtils;
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
import java.time.Duration;
import java.util.regex.Pattern;

import static comp4111.dal.DatabaseInfo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TransactionTimeoutIntegrationTest extends AbstractServerTest {

    private ObjectMapper objectMapper;
    private String token;
    private Long bookId3;
    private Long transactionId;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        assumeTrue(() -> {
            try (@SuppressWarnings("unused") var con = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD)) {
                return true;
            } catch (Throwable tr) {
                return false;
            }
        }, "Database not started; Skipping live integration tests");

        DatabaseUtils.setupSchemas(true);
        DatabaseUtils.createDefaultUsers();
        DatabaseConnectionPoolV2.getInstance().setDefaultTxTimeout(Duration.ofSeconds(1));

        {
            final var handlers = new HttpAsyncPathHandler[MainApplication.PATTERN_HANDLER.size()];
            MainApplication.PATTERN_HANDLER.values().toArray(handlers);
            registerAndStartServer(handlers);
        }

        objectMapper = JacksonUtils.getDefaultObjectMapper();
    }

    void logInFirst() throws Exception {
        final var loginRequest = new LoginRequest("user00001", "pass00001");
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
                "SINoALICE",
                "SQUARE",
                "ENIX",
                2017
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assumeTrue(HttpStatus.SC_CREATED == response.getCode());

            final var pattern = Pattern.compile("/books/(.+)");
            final var matcher = pattern.matcher(response.getHeader("Location").getValue());
            assumeTrue(matcher.matches());
            bookId3 = Long.parseLong(matcher.group(1));
        }
    }

    void markBookAsLoaned() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": false}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId3, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void requestATransactionId() throws Exception {
        final var entity = new StringEntity("");

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var transactionId = objectMapper.readTree(responseJson).get("Transaction").longValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.transactionId = transactionId;
        }
    }

    void pushAValidActionIntoATransaction() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId, bookId3, TransactionPutRequest.Action.RETURN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void commitTheTransaction() throws Exception {
        final var transaction = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.COMMIT);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
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
        markBookAsLoaned();
        requestATransactionId();
        pushAValidActionIntoATransaction();
        Thread.sleep(2_000);
        commitTheTransaction();
        logOutWithTheCorrectToken();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null);
        }
        super.tearDown();

        DatabaseUtils.dropDatabase();
        DatabaseConnectionPoolV2.getInstance().close();

        token = null;
        objectMapper = null;
    }
}
