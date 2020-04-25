package comp4111.model;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.util.JacksonUtils;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionPostRequestTest {

    private Random random;
    private ObjectMapper objectMapper;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        random = new Random();
        objectMapper = JacksonUtils.getDefaultObjectMapper();
        transactionId = (long) random.nextInt(Integer.MAX_VALUE);
    }

    @Test
    void givenEmptyJson_checkThrows() {
        @Language("JSON") final var json = "{}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenJsonMissingTransaction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Operation\": \"commit\"" +
                "}";

        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenJsonMissingOperation_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  \"" + transactionId + "\"" +
                "}";

        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenJsonBadOperation_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  \"" + transactionId + "\", " +
                "\"Operation\": \"dance\"" +
                "}";

        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenJsonNullTransaction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  null, " +
                "\"Operation\": \"commit\"" +
                "}";

        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenJsonNullOperation_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  \"" + transactionId + "\", " +
                "\"Operation\": null" +
                "}";

        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, TransactionPostRequest.class));
    }

    @Test
    void givenCommitJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  " + transactionId + ", " +
                "\"Operation\": \"commit\"" +
                "}";
        final var expected = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.COMMIT);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPostRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getOperation(), actual.getOperation());
    }

    @Test
    void givenCancelJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  " + transactionId + ", " +
                "\"Operation\": \"cancel\"" +
                "}";
        final var expected = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.CANCEL);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPostRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getOperation(), actual.getOperation());
    }

    @AfterEach
    void tearDown() {
        transactionId = null;
        objectMapper = null;
        random = null;
    }
}
