package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class TransactionPostResult {

    @NotNull
    @JsonProperty("Transaction")
    private final Long transaction;

    public TransactionPostResult(@NotNull @JsonProperty("Transaction") Long id) {
        this.transaction = id;
    }
}
