package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BooksGetResult {

    @JsonProperty("FoundBooks")
    @NotNull
    private final int foundBooks;

    @JsonProperty("Results")
    @NotNull
    private final List<Book> results = new ArrayList<>();

    public BooksGetResult(@NotNull List<Book> results) {
        foundBooks = results.size();
        this.results.addAll(results);
    }

    public int getFoundBooks() {
        return foundBooks;
    }
}
