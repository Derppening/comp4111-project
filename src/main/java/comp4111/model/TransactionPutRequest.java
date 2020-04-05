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

    @NotNull
    @JsonProperty("Transaction")
    private final Long transaction;
    @JsonProperty("Book")
    private final long id;
    @JsonProperty("Action")
    @NotNull
    private final Action action;

    public TransactionPutRequest(
            @NotNull @JsonProperty("Transaction") Long transaction,
            @JsonProperty("Book") long bookId,
            @NotNull @JsonProperty("Action") Action action
    ) {
        this.transaction = transaction;
        this.id = bookId;
        this.action = Objects.requireNonNull(action);
    }

    @NotNull
    public Long getTransaction() {
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
