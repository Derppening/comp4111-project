package comp4111.controller;

import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPutRequest;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manager for caching transaction requests.
 */
public class TransactionManager {

    private final Map<@NotNull UUID, @NotNull List<TransactionPutRequest>> inFlightTransactions = Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates a new transaction.
     *
     * @return UUID of the transaction.
     */
    @NotNull
    public UUID newTransaction() {
        final var uuid = UUID.randomUUID();
        assert !inFlightTransactions.containsKey(uuid);
        inFlightTransactions.put(uuid, Collections.synchronizedList(new ArrayList<>()));

        return uuid;
    }

    /**
     * Adds a plan to the given transaction.
     *
     * @param putRequest The PUT request of the transaction.
     * @return {@code true} if the operation was successful.
     */
    public boolean addTransactionPlan(@NotNull final TransactionPutRequest putRequest) {
        final var uuid = putRequest.getTransaction();

        final var transaction = inFlightTransactions.get(uuid);
        if (transaction == null) {
            return false;
        }

        transaction.add(putRequest);
        return true;
    }

    /**
     * Commits or aborts the given transaction.
     *
     * @param postRequest The POST request of the transaction.
     * @return {@code true} if the operation was successful.
     */
    public boolean performTransaction(@NotNull final TransactionPostRequest postRequest) {
        final var uuid = postRequest.getTransaction();

        final var transaction = inFlightTransactions.get(uuid);
        if (transaction == null) {
            return false;
        }
        inFlightTransactions.remove(uuid);

        if (postRequest.getOperation() == TransactionPostRequest.Operation.COMMIT) {
            // TODO: Commit to database
        } else {
            // no-op
        }

        return true;
    }
}
