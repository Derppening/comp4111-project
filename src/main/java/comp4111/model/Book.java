package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class Book {

    @JsonProperty("Title")
    @NotNull
    private final String title;

    @JsonProperty("Author")
    @NotNull
    private final String author;

    @JsonProperty("Publisher")
    @NotNull
    private final String publisher;

    @JsonProperty("Year")
    private final int year;

    public Book(
            @NotNull @JsonProperty("Title") String title,
            @NotNull @JsonProperty("Author") String author,
            @NotNull @JsonProperty("Publisher") String publisher,
            @JsonProperty("Year") int year) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getAuthor() {
        return author;
    }

    @NotNull
    public String getPublisher() {
        return publisher;
    }

    public int getYear() {
        return year;
    }
}
