package comp4111.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionPostRequestTest {

    private ObjectMapper objectMapper;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        uuid = UUID.randomUUID();
    }

    @Test
    void givenCommitJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  \"" + uuid.toString() +"\", " +
                "\"Operation\": \"commit\"" +
                "}";
        final var expected = new TransactionPostRequest(uuid, TransactionPostRequest.Operation.COMMIT);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPostRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getOperation(), actual.getOperation());
    }

    @Test
    void givenCancelJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Transaction\":  \"" + uuid.toString() +"\", " +
                "\"Operation\": \"cancel\"" +
                "}";
        final var expected = new TransactionPostRequest(uuid, TransactionPostRequest.Operation.CANCEL);
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, TransactionPostRequest.class));

        assertEquals(expected.getTransaction(), actual.getTransaction());
        assertEquals(expected.getOperation(), actual.getOperation());
    }

    @AfterEach
    void tearDown() {
        uuid = null;
        objectMapper = null;
    }
}
