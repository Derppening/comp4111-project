package comp4111.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionPostResultTest {

    private Random random;
    private ObjectMapper objectMapper;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        random = new Random();
        objectMapper = new ObjectMapper();
        transactionId = (long) random.nextInt(Integer.MAX_VALUE);
    }

    @Test
    void givenObj_checkCanSerialize() throws JsonProcessingException {
        final var obj = new TransactionPostResult(transactionId);

        final var actualJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(obj));

        final var reparsedJson = objectMapper.readTree(actualJson);

        assertTrue(reparsedJson.has("Transaction"));

        final var transactionNode = reparsedJson.get("Transaction");

        assertTrue(transactionNode.isIntegralNumber());

        assertEquals(transactionId, transactionNode.longValue());
    }

    @AfterEach
    void tearDown() {
        transactionId = null;
        objectMapper = null;
        random = null;
    }
}
