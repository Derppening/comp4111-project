package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TransactionPutRequest {

    public enum Action {
        LOAN, RETURN;

        public static Action normalizedValueOf(@NotNull String value) {
            return valueOf(value.toUpperCase());
        }
    }

    @JsonProperty("Transaction")
    @NotNull
    private final UUID transaction;
    @JsonProperty("Book")
    private final int id;
    @JsonProperty("Action")
    private final Action action;

    public TransactionPutRequest(
            @NotNull @JsonProperty("Transaction") UUID uuid,
            @JsonProperty("Book") int bookId,
            @JsonProperty("Action") String action
    ) {
        this.transaction = uuid;
        this.id = bookId;
        this.action = Action.normalizedValueOf(action);
    }

    @NotNull
    public UUID getTransaction() {
        return transaction;
    }

    public int getId() {
        return id;
    }

    public Action getAction() {
        return action;
    }
}
