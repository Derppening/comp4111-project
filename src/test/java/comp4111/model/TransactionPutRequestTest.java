package comp4111.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionPutRequestTest {

    private ObjectMapper objectMapper;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        uuid = UUID.randomUUID();
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
