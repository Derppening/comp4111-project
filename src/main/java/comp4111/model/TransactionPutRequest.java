package comp4111.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TransactionPutRequest {

    public enum Action {
        LOAN, RETURN;

        @JsonCreator
        public static Action setValue(@NotNull String key) {
            return normalizedValueOf(key);
        }

        private static Action normalizedValueOf(@NotNull String value) {
            return valueOf(value.toUpperCase());
        }
    }

    @JsonProperty("Transaction")
    private final int transaction;
    @JsonProperty("Book")
    private final long id;
    @JsonProperty("Action")
    @NotNull
    private final Action action;

    public TransactionPutRequest(
            @JsonProperty("Transaction") int transaction,
            @JsonProperty("Book") long bookId,
            @NotNull @JsonProperty("Action") Action action
    ) {
        this.transaction = transaction;
        this.id = bookId;
        this.action = Objects.requireNonNull(action);
    }

    public int getTransaction() {
        return transaction;
    }

    public long getId() {
        return id;
    }

    @NotNull
    public Action getAction() {
        return action;
    }
}
