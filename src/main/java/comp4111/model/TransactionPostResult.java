package comp4111.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionPostResult {

    @JsonProperty("Transaction")
    private final int transaction;

    public TransactionPostResult(@JsonProperty("Transaction") int id) {
        this.transaction = id;
    }
}
