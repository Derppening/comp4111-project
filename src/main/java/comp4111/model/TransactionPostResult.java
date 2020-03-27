package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TransactionPostResult {

    @JsonProperty("Transaction")
    @NotNull
    private final UUID transaction;

    public TransactionPostResult(@NotNull @JsonProperty("Transaction") UUID uuid) {
        this.transaction = uuid;
    }
}
