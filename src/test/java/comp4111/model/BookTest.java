package comp4111.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenJson_checkCanDeserialize() {
        @Language("JSON") final var json = "{" +
                "\"Title\": \"Alice in Wonderland\", " +
                "\"Author\": \"Lewis Carroll\", " +
                "\"Publisher\": \"Macmillan Publishers\", " +
                "\"Year\": 1865" +
                "}";
        final var expected = new Book(
                "Alice in Wonderland",
                "Lewis Carroll",
                "Macmillan Publishers",
                1865
        );
        final var actual = assertDoesNotThrow(() -> objectMapper.readValue(json, Book.class));

        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getPublisher(), actual.getPublisher());
        assertEquals(expected.getYear(), actual.getYear());
    }

    @Test
    void givenObj_checkCanSerialize() throws JsonProcessingException {
        final var expectedTitle = "Alice in Wonderland";
        final var expectedAuthor = "Lewis Carroll";
        final var expectedPublisher = "Macmillan Publishers";
        final var expectedYear = 1865;

        final var obj = new Book(
                expectedTitle,
                expectedAuthor,
                expectedPublisher,
                expectedYear
        );

        final var actualJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(obj));

        final var reparsedJson = objectMapper.readTree(actualJson);

        assertTrue(reparsedJson.has("Title"));
        assertTrue(reparsedJson.has("Author"));
        assertTrue(reparsedJson.has("Publisher"));
        assertTrue(reparsedJson.has("Year"));

        final var titleNode = reparsedJson.get("Title");
        final var authorNode = reparsedJson.get("Author");
        final var publisherNode = reparsedJson.get("Publisher");
        final var yearNode = reparsedJson.get("Year");

        assertTrue(titleNode.isTextual());
        assertTrue(authorNode.isTextual());
        assertTrue(publisherNode.isTextual());
        assertTrue(yearNode.isIntegralNumber());

        assertEquals(expectedTitle, titleNode.textValue());
        assertEquals(expectedAuthor, authorNode.textValue());
        assertEquals(expectedPublisher, publisherNode.textValue());
        assertEquals(expectedYear, yearNode.intValue());
    }

    @AfterEach
    void tearDown() {
        objectMapper = null;
    }
}
