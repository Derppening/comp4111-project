package comp4111.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionPostResultTest {

    private ObjectMapper objectMapper;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        uuid = UUID.randomUUID();
    }

    @Test
    void givenObj_checkCanSerialize() throws JsonProcessingException {
        final var obj = new TransactionPostResult(uuid);

        final var actualJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(obj));

        final var reparsedJson = objectMapper.readTree(actualJson);

        assertTrue(reparsedJson.has("Transaction"));

        final var transactionNode = reparsedJson.get("Transaction");

        assertTrue(transactionNode.isTextual());

        assertEquals(uuid.toString(), transactionNode.textValue());
    }

    @AfterEach
    void tearDown() {
        uuid = null;
        objectMapper = null;
    }
}
