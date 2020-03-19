package comp4111.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{\"Username\": \"user001\", \"Password\": \"pass001\"}";
        final var expected = new LoginRequest("user001", "pass001");
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, LoginRequest.class));

        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
    }

    @Test
    void givenObj_checkCanSerialize() throws JsonProcessingException {
        final var expectedUsername = "user001";
        final var expectedPassword = "pass001";

        final var obj = new LoginRequest(expectedUsername, expectedPassword);

        final var actualJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(obj));

        final var reparsedJson = objectMapper.readTree(actualJson);

        assertTrue(reparsedJson.has("Username"));
        assertTrue(reparsedJson.has("Password"));

        final var usernameNode = reparsedJson.get("Username");
        final var passwordNode = reparsedJson.get("Password");

        assertTrue(usernameNode.isTextual());
        assertTrue(passwordNode.isTextual());
        assertEquals(expectedUsername, usernameNode.textValue());
        assertEquals(expectedPassword, passwordNode.textValue());
    }

    @AfterEach
    void tearDown() {
        objectMapper = null;
    }
}
