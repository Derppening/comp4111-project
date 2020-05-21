package comp4111;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.DatabaseConnectionPoolV2;
import comp4111.dal.DatabaseUtils;
import comp4111.handler.*;
import comp4111.model.Book;
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

import static comp4111.dal.DatabaseInfo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class BookOperationsAndTransactionIntegrationTest extends AbstractServerTest {

    private ObjectMapper objectMapper;
    private String token;
    private Long bookId1;
    private Long bookId2;
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

        {
            final var handlers = new HttpAsyncPathHandler[MainApplication.PATTERN_HANDLER.size()];
            MainApplication.PATTERN_HANDLER.values().toArray(handlers);
            registerAndStartServer(handlers);
        }

        objectMapper = JacksonUtils.getDefaultObjectMapper();
    }

    void pre_LoginFirst() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Username\": \"user00001\", " +
                "\"Password\": \"pass00001\"" +
                "}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, LoginHandler.HANDLE_PATTERN, entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var token = objectMapper.readTree(responseJson).get("Token").textValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.token = token;
        }
    }

    void h_AddABookThatDoesNotExist() throws Exception {
        final var book = new Book(
                "Alice in Wonderland",
                "Lewis Carroll",
                "Macmillan Publishers",
                1865
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_CREATED, response.getCode());

            final var pattern = Pattern.compile("/books/(.+)");
            final var matcher = pattern.matcher(response.getHeader("Location").getValue());
            assertTrue(matcher.matches());
            bookId1 = Long.parseLong(matcher.group(1));
        }
    }

    void i_AddABookWithTheSameInformationAgain() throws Exception {
        final var book = new Book(
                "Alice in Wonderland",
                "Lewis Carroll",
                "Macmillan Publishers",
                1865
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_CONFLICT, response.getCode());
        }
    }

    void i_ViewTheAddedBook() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_AddBook2() throws Exception {
        final var book = new Book(
                "Alice",
                "Lewis",
                "Mac",
                2020
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_CREATED, response.getCode());

            final var pattern = Pattern.compile("/books/(.+)");
            final var matcher = pattern.matcher(response.getHeader("Location").getValue());
            assertTrue(matcher.matches());
            bookId2 = Long.parseLong(matcher.group(1));
        }
    }

    void j_AddBook3() throws Exception {
        final var book = new Book(
                "SINoALICE",
                "SQUARE",
                "ENIX",
                2017
        );
        @Language("JSON") final var payload = objectMapper.writeValueAsString(book);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_CREATED, response.getCode());

            final var pattern = Pattern.compile("/books/(.+)");
            final var matcher = pattern.matcher(response.getHeader("Location").getValue());
            assertTrue(matcher.matches());
            bookId3 = Long.parseLong(matcher.group(1));
        }
    }

    void j_LookUpTheBookInformationWithMultipleBookRecords_Sorted() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?sortby=year&order=desc&limit=2&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra1_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra2_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?id=0&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
        }
    }

    void j_Extra3_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?id=%d&token=%s", BooksHandler.HANDLE_PATTERN, bookId2, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra4_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?title=Alice&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra5_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?author=Lewis&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra6_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?id=%d&author=square&token=%s", BooksHandler.HANDLE_PATTERN, bookId2, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra7_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?sortby=id&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra8_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?sortby=ids&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void j_Extra9_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?order=desc&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void j_Extra10_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?sortby=id&order=desc&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra11_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?sortby=ids&order=desc&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void j_Extra12_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?title=Alice&author=Lewis&sortby=id&order=desc&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void j_Extra13_LookUpTheBookInformation() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?title=Alice&author=Lewis&sortby=id&order=desc&limit=1&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void k_LoanTheBookAddedInH() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": false}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void l_LoanTheBookAddedInHAgain() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": false}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void m_LoanABookThatDoesNotExist() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": false}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/50000000?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
            assertEquals("No book record", response.getReasonPhrase());
        }
    }

    void n_ReturnTheBookAddedInH() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": true}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void o_ReturnTheBookAddedInHAgain() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": true}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void p_ReturnABookThatDoesNotExist() throws Exception {
        @Language("JSON") final var payload = "{\"Available\": true}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/577777777?token=%s", BooksHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
            assertEquals("No book record", response.getReasonPhrase());
        }
    }

    void q_DeleteAnExistingBookAddedInH() throws Exception {
        try (var response = makeRequest(Method.DELETE, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void r_DeleteAnExistingBookAddedInH() throws Exception {
        try (var response = makeRequest(Method.DELETE, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId1, token), null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
            assertEquals("No book record", response.getReasonPhrase());
        }
    }

    void s_RequestATransactionID() throws Exception {
        final var entity = new StringEntity("");

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var transactionId = objectMapper.readTree(responseJson).get("Transaction").longValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.transactionId = transactionId;
        }
    }

    void t_PushValidActionsIntoATransaction1() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId, bookId3, TransactionPutRequest.Action.LOAN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void t_PushValidActionsIntoATransaction2() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId, bookId2, TransactionPutRequest.Action.LOAN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void t_PushValidActionsIntoATransaction3() throws Exception {
        final var transaction = new TransactionPutRequest(transactionId, bookId3, TransactionPutRequest.Action.RETURN);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void u_PushInvalidActionsIntoATransaction() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": " + transactionId + ", " +
                "\"Book\": " + bookId3 + ", " +
                "\"Action\": \"logout\"" +
                "}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void v_CommitTheTransactionWithIDObtainedInS() throws Exception {
        final var transaction = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.COMMIT);
        @Language("JSON") final var payload = objectMapper.writeValueAsString(transaction);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void w_CommitTheTransactionWithAnInvalidID() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Transaction\": 22323950384, " +
                "\"Operation\": \"commit\"" +
                "}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, String.format("%s?token=%s", TransactionHandler.HANDLE_PATTERN, token), entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void x_VerifyTheResultOfTransactionActionsInT() throws Exception {
        @Language("JSON") var payload = "{\"Available\": false}";
        var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId3, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }

        payload = "{\"Available\": true}";
        entity = new StringEntity(payload);
        try (var response = makeRequest(Method.PUT, String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId2, token), entity)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void post_LogOutWithTheCorrectToken() throws Exception {
        try (var response = makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null)) {

            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    @Test
    void mainTest() throws Exception {
        pre_LoginFirst();
        h_AddABookThatDoesNotExist();
        i_AddABookWithTheSameInformationAgain();
        i_ViewTheAddedBook();
        j_AddBook2();
        j_AddBook3();
        j_LookUpTheBookInformationWithMultipleBookRecords_Sorted();
        j_Extra1_LookUpTheBookInformation();
        j_Extra2_LookUpTheBookInformation();
        j_Extra3_LookUpTheBookInformation();
        j_Extra4_LookUpTheBookInformation();
        j_Extra5_LookUpTheBookInformation();
        j_Extra6_LookUpTheBookInformation();
        j_Extra7_LookUpTheBookInformation();
        j_Extra8_LookUpTheBookInformation();
        j_Extra9_LookUpTheBookInformation();
        j_Extra10_LookUpTheBookInformation();
        j_Extra11_LookUpTheBookInformation();
        j_Extra12_LookUpTheBookInformation();
        j_Extra13_LookUpTheBookInformation();
        k_LoanTheBookAddedInH();
        l_LoanTheBookAddedInHAgain();
        m_LoanABookThatDoesNotExist();
        n_ReturnTheBookAddedInH();
        o_ReturnTheBookAddedInHAgain();
        p_ReturnABookThatDoesNotExist();
        q_DeleteAnExistingBookAddedInH();
        r_DeleteAnExistingBookAddedInH();
        s_RequestATransactionID();
        t_PushValidActionsIntoATransaction1();
        t_PushValidActionsIntoATransaction2();
        t_PushValidActionsIntoATransaction3();
        u_PushInvalidActionsIntoATransaction();
        v_CommitTheTransactionWithIDObtainedInS();
        w_CommitTheTransactionWithAnInvalidID();
        x_VerifyTheResultOfTransactionActionsInT();
        post_LogOutWithTheCorrectToken();
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
