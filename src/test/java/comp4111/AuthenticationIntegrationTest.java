package comp4111;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.dal.DatabaseConnection;
import comp4111.handler.*;
import comp4111.model.LoginRequest;
import comp4111.util.JacksonUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AuthenticationIntegrationTest extends AbstractServerTest {

    private ObjectMapper objectMapper;
    private String token;

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

    void a_LoginWithTheCorrectUsernameAndPassword() throws Exception {
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

    void b_LoginWithTheCorrectUsernameAndPasswordAgain() throws Exception {
        final var loginRequest = new LoginRequest("user001", "pass001");
        @Language("JSON") final var payload = objectMapper.writeValueAsString(loginRequest);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, LoginHandler.HANDLE_PATTERN, entity)) {
            assertEquals(HttpStatus.SC_CONFLICT, response.getCode());
        }
    }

    void c_LoginWithTheIncorrectUsernameAndPassword() throws Exception {
        final var loginRequest = new LoginRequest("user001", "pass002");
        @Language("JSON") final var payload = objectMapper.writeValueAsString(loginRequest);
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, LoginHandler.HANDLE_PATTERN, entity)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void d_LogoutWithTheCorrectTokenObtainedFromA() throws Exception {
        try (var response = makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());
        }
    }

    void e_LogoutWithTheExpiredTokenAgain() throws Exception {
        try (var response = makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=" + token, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void f_LogoutWithAnIncorrectToken() throws Exception {
        try (var response = makeRequest(Method.GET, LogoutHandler.HANDLE_PATTERN + "?token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void g_PerformOtherActionsWithAnIncorrectToken_AddBook() throws Exception {
        try (var response = makeRequest(Method.POST, BooksHandler.HANDLE_PATTERN + "?token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void g_PerformOtherActionsWithAnIncorrectToken_SortBook() throws Exception {
        try (var response = makeRequest(Method.GET, BooksHandler.HANDLE_PATTERN + "?sortby=id&order=desc&limit=10&token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void g_PerformOtherActionsWithAnIncorrectToken_LoanOrReturnBook() throws Exception {
        try (var response = makeRequest(Method.PUT, BooksHandler.HANDLE_PATTERN + "/1?token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void g_PerformOtherActionsWithAnIncorrectToken_DeleteBook() throws Exception {
        try (var response = makeRequest(Method.DELETE, BooksHandler.HANDLE_PATTERN + "/1?token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    void g_PerformOtherActionsWithAnIncorrectToken_CommitTransaction() throws Exception {
        try (var response = makeRequest(Method.POST, TransactionHandler.HANDLE_PATTERN + "?token=deadbeef", null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
        }
    }

    @Test
    void mainTest() throws Exception {
        a_LoginWithTheCorrectUsernameAndPassword();
        b_LoginWithTheCorrectUsernameAndPasswordAgain();
        c_LoginWithTheIncorrectUsernameAndPassword();
        d_LogoutWithTheCorrectTokenObtainedFromA();
        e_LogoutWithTheExpiredTokenAgain();
        f_LogoutWithAnIncorrectToken();
        g_PerformOtherActionsWithAnIncorrectToken_AddBook();
        g_PerformOtherActionsWithAnIncorrectToken_SortBook();
        g_PerformOtherActionsWithAnIncorrectToken_LoanOrReturnBook();
        g_PerformOtherActionsWithAnIncorrectToken_DeleteBook();
        g_PerformOtherActionsWithAnIncorrectToken_CommitTransaction();
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
