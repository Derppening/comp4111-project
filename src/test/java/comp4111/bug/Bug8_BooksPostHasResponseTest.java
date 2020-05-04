package comp4111.bug;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.AbstractServerTest;
import comp4111.DatabaseUtils;
import comp4111.MainApplication;
import comp4111.dal.DatabaseConnection;
import comp4111.handler.BooksHandler;
import comp4111.handler.HttpAsyncPathHandler;
import comp4111.handler.LoginHandler;
import comp4111.handler.LogoutHandler;
import comp4111.model.Book;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Bug8_BooksPostHasResponseTest extends AbstractServerTest {

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
            final var handlers = new HttpAsyncPathHandler[MainApplication.PATTERN_HANDLER.size()];
            MainApplication.PATTERN_HANDLER.values().toArray(handlers);
            registerAndStartServer(handlers);
        }

        objectMapper = JacksonUtils.getDefaultObjectMapper();
    }

    void pre_LoginFirst() throws Exception {
        @Language("JSON") final var payload = "{" +
                "\"Username\": \"user001\", " +
                "\"Password\": \"pass001\"" +
                "}";
        final var entity = new StringEntity(payload);

        try (var response = makeRequest(Method.POST, LoginHandler.HANDLE_PATTERN, entity)) {
            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var token = objectMapper.readTree(responseJson).get("Token").textValue();

            assertEquals(HttpStatus.SC_OK, response.getCode());
            this.token = token;
        }
    }

    void a_AddBook() throws Exception {
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
            final long bookId = Long.parseLong(matcher.group(1));

            assertNotNull(response.getEntity());
            assertNotEquals(0, response.getEntity().getContentLength());
            assertTrue(() -> {
                final var expectedSubstring = String.format("%s/%d?token=%s", BooksHandler.HANDLE_PATTERN, bookId, token);
                final var actualResponse = assertDoesNotThrow(() -> new String(response.getEntity().getContent().readAllBytes()));

                return actualResponse.contains(expectedSubstring);
            });
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
        post_LogOutWithTheCorrectToken();
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
