package comp4111.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
    @NotNull
    private final Operation operation;

    public TransactionPostRequest(
            @NotNull @JsonProperty("Transaction") UUID uuid,
            @NotNull @JsonProperty("Operation") Operation operation
    ) {
        this.transaction = Objects.requireNonNull(uuid);
        this.operation = Objects.requireNonNull(operation);
    }

    @NotNull
    public UUID getTransaction() {
        return transaction;
    }

    @NotNull
    public Operation getOperation() {
        return operation;
    }
}
