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

/* https://github.com/Derppening/comp4111-project/issues/9 */
public class Bug9_GetBooksSmallerThanLimitTest extends AbstractServerTest {

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

    void a_AddBook1() throws Exception {
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
        }
    }

    void a_AddBook2() throws Exception {
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
        }
    }

    void a_AddBook3() throws Exception {
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
        }
    }

    void b_LookUpTheBookInformationWithGreaterLimit() throws Exception {
        try (var response = makeRequest(Method.GET, String.format("%s?limit=4&token=%s", BooksHandler.HANDLE_PATTERN, token), null)) {
            assertEquals(HttpStatus.SC_OK, response.getCode());

            final var responseJson = response.getEntity().getContent().readAllBytes();
            final var responseTree = objectMapper.readTree(responseJson);
            final var results = responseTree.get("Results");
            assertNotNull(results);
            assertTrue(results.isArray());
            assertEquals(3, results.size());
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
        a_AddBook1();
        a_AddBook2();
        a_AddBook3();
        b_LookUpTheBookInformationWithGreaterLimit();
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
