package comp4111.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TransactionPostRequest {

    public enum Operation {
        COMMIT, CANCEL;

        @JsonCreator
        public static Operation setValue(@NotNull String key) {
            return normalizedValueOf(key);
        }

        private static Operation normalizedValueOf(@NotNull String value) {
            return valueOf(value.toUpperCase());
        }
    }

    @JsonProperty("Transaction")
    @NotNull
    private final UUID transaction;
    @JsonProperty("Operation")
    private final Operation operation;

    public TransactionPostRequest(
            @NotNull @JsonProperty("Transaction") UUID uuid,
            @JsonProperty("Operation") Operation operation
    ) {
        this.transaction = uuid;
        this.operation = operation;
    }

    @NotNull
    public UUID getTransaction() {
        return transaction;
    }

    public Operation getOperation() {
        return operation;
    }
}
