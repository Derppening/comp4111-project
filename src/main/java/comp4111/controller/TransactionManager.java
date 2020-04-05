package comp4111.controller;

import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPutRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Manager for caching transaction requests.
 */
public class TransactionManager {

    static final Supplier<Map<UUID, List<TransactionPutRequest>>> DEFAULT_MAP_SUPPLIER = () -> Collections.synchronizedMap(new HashMap<>());
    static final Supplier<List<TransactionPutRequest>> DEFAULT_TRANSACTION_LIST_SUPPLIER = () -> Collections.synchronizedList(new ArrayList<>());

    @Nullable
    private static TransactionManager INSTANCE;

    /**
     * @return The singleton instance of this class.
     */
    @NotNull
    public synchronized static TransactionManager getInstance() {
        return getInstance(null, null);
    }

    /**
     * This method is for cases where a custom {@link Map} or {@link Supplier} is required for backing the transaction
     * map, such as for mocking and testing.
     *
     * @param backingMap The map to use for storing transaction IDs.
     * @param listCreator The supplier to use for creating lists to store transaction details.
     * @return The singleton instance of this class.
     */
    @NotNull
    synchronized static TransactionManager getInstance(@Nullable Map<UUID, List<TransactionPutRequest>> backingMap, @Nullable Supplier<List<TransactionPutRequest>> listCreator) {
        if (INSTANCE == null) {
            final var map = backingMap != null ? backingMap : DEFAULT_MAP_SUPPLIER.get();
            final var listSupplier = listCreator != null ? listCreator : DEFAULT_TRANSACTION_LIST_SUPPLIER;

            INSTANCE = new TransactionManager(map, listSupplier);
        }

        return INSTANCE;
    }

    @NotNull
    private final Map<@NotNull UUID, @NotNull List<TransactionPutRequest>> inFlightTransactions;
    @NotNull
    private final Supplier<@NotNull List<@NotNull TransactionPutRequest>> listCreator;

    TransactionManager(@NotNull Map<UUID, List<TransactionPutRequest>> backingMap, @NotNull Supplier<List<TransactionPutRequest>> listCreator) {
        this.inFlightTransactions = backingMap;
        this.listCreator = listCreator;
    }

    /**
     * Creates a new transaction.
     *
     * @return UUID of the transaction.
     */
    @NotNull
    public synchronized UUID newTransaction() {
        final var uuid = UUID.randomUUID();
        inFlightTransactions.put(uuid, listCreator.get());

        return uuid;
    }

    /**
     * Adds a plan to the given transaction.
     *
     * @param putRequest The PUT request of the transaction.
     * @return {@code true} if the operation was successful.
     */
    public synchronized boolean addTransactionPlan(@NotNull final TransactionPutRequest putRequest) {
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
     * @deprecated Use {@link TransactionManager#getAndEraseTransaction(TransactionPostRequest)} and handle the
     * transaction in the calling method.
     */
    @Deprecated
    public synchronized boolean performTransaction(@NotNull final TransactionPostRequest postRequest) {
        final var transaction = getAndEraseTransaction(postRequest);
        if (transaction == null) {
            return false;
        }

        if (transaction.isEmpty()) {
            return true;
        }

        if (postRequest.getOperation() == TransactionPostRequest.Operation.COMMIT) {
            // TODO: Commit to database
        } else {
            // no-op
        }

        return true;
    }

    /**
     * Returns the transaction given by the transaction ID, and clears it from this manager.
     *
     * @param postRequest The POST request of the transaction.
     * @return The transaction associated with the POST request, or {@code null} if the transaction does not exist.
     */
    @Nullable
    public synchronized List<TransactionPutRequest> getAndEraseTransaction(@NotNull final TransactionPostRequest postRequest) {
        final var uuid = postRequest.getTransaction();

        return inFlightTransactions.remove(uuid);
    }
}
