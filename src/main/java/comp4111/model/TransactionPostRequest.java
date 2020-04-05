package comp4111.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
    private final int transaction;
    @JsonProperty("Operation")
    @NotNull
    private final Operation operation;

    public TransactionPostRequest(
            @JsonProperty("Transaction") int id,
            @NotNull @JsonProperty("Operation") Operation operation
    ) {
        this.transaction = id;
        this.operation = Objects.requireNonNull(operation);
    }

    public int getTransaction() {
        return transaction;
    }

    @NotNull
    public Operation getOperation() {
        return operation;
    }
}
