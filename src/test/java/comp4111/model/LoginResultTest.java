package comp4111.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginResultTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenObj_checkCanSerialize() throws JsonProcessingException {
        final var expectedToken = "asf3219";

        final var obj = new LoginResult(expectedToken);

        final var actualJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(obj));

        final var reparsedJson = objectMapper.readTree(actualJson);

        assertTrue(reparsedJson.has("Token"));

        final var tokenNode = reparsedJson.get("Token");

        assertTrue(tokenNode.isTextual());

        assertEquals(expectedToken, tokenNode.textValue());
    }

    @AfterEach
    void tearDown() {
        objectMapper = null;
    }
}
