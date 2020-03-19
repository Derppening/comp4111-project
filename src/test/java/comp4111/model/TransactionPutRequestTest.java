package comp4111.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import comp4111.util.JacksonUtils;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionPutRequestTest {

    private ObjectMapper objectMapper;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        objectMapper = JacksonUtils.getDefaultObjectMapper();
        uuid = UUID.randomUUID();
    }

    @Test
    void givenEmptyJson_checkThrows() {
        @Language("JSON") final var json = "{}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonMissingTransaction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Book\": 1, " +
                "\"Action\": \"loan\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonMissingBook_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Action\": \"loan\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonMissingAction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": 1" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonBadBookType_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": \"abc\", " +
                "\"Action\": \"loan\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonBadActionType_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": 1, " +
                "\"Action\": \"dance\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonNullTransaction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": null, " +
                "\"Book\": \"abc\", " +
                "\"Action\": \"loan\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonNullBook_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": null, " +
                "\"Action\": \"loan\"" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenJsonNullAction_checkThrows() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": 1, " +
                "\"Action\": null" +
                "}";

        assertThrows(Exception.class, () -> objectMapper.readValue(json, TransactionPutRequest.class));
    }

    @Test
    void givenLoanJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": 1, " +
                "\"Action\": \"loan\"" +
                "}";
        final var expected = new TransactionPutRequest(uuid, 1, TransactionPutRequest.Action.LOAN);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPutRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAction(), actual.getAction());
    }

    @Test
    void givenReturnJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\": \"" + uuid.toString() + "\", " +
                "\"Book\": 1, " +
                "\"Action\": \"return\"" +
                "}";
        final var expected = new TransactionPutRequest(uuid, 1, TransactionPutRequest.Action.RETURN);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPutRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAction(), actual.getAction());
    }

    @AfterEach
    void tearDown() {
        uuid = null;
        objectMapper = null;
    }
}
