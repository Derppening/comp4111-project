package comp4111.controller;

import comp4111.model.TransactionPostRequest;
import comp4111.model.TransactionPutRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TransactionManagerTest {

    private Random random;
    private Map<Long, List<TransactionPutRequest>> backingMap;
    private Supplier<List<TransactionPutRequest>> transactionListSupplier;
    private TransactionManager transactionMgr;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        random = new Random();
        backingMap = TransactionManager.DEFAULT_MAP_SUPPLIER.get();
        transactionListSupplier = TransactionManager.DEFAULT_TRANSACTION_LIST_SUPPLIER;
        transactionMgr = new TransactionManager(backingMap, transactionListSupplier);
    }

    @Test
    void tryCreateTransaction() {
        final var transaction = transactionMgr.newTransaction();

        assertNotNull(transaction);
        assertEquals(1, backingMap.size());
    }

    @Test
    void givenOneTokenExists_whenCreateToken_assertNewTokenCreated() {
        final var transaction1 = transactionMgr.newTransaction();
        final var transaction2 = transactionMgr.newTransaction();

        assertEquals(2, backingMap.size());
        assertNotEquals(transaction1, transaction2);
    }

    @Test
    void givenToken_tryAddLoanTransaction() {
        final var transaction = transactionMgr.newTransaction();

        final var request = new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.LOAN);
        assertTrue(transactionMgr.addTransactionPlan(request));

        final var plan = backingMap.get(transaction);
        assertNotNull(plan);
        assertEquals(1, plan.size());

        final var actualRequest = plan.get(0);
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
        assertEquals(request.getId(), actualRequest.getId());
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
    }

    @Test
    void givenToken_tryAddReturnTransaction() {
        final var transaction = transactionMgr.newTransaction();

        final var request = new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.RETURN);
        assertTrue(transactionMgr.addTransactionPlan(request));

        final var plan = backingMap.get(transaction);
        assertNotNull(plan);
        assertEquals(1, plan.size());

        final var actualRequest = plan.get(0);
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
        assertEquals(request.getId(), actualRequest.getId());
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
    }

    @Test
    void givenNonEmptyTransactionList_tryAddTransaction() {
        final var transaction = transactionMgr.newTransaction();

        assumeTrue(transactionMgr.addTransactionPlan(new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.RETURN)));

        final var request = new TransactionPutRequest(transaction, 2, TransactionPutRequest.Action.RETURN);
        assertTrue(transactionMgr.addTransactionPlan(request));

        final var plan = backingMap.get(transaction);
        assertNotNull(plan);
        assertEquals(2, plan.size());

        final var actualRequest = plan.get(1);
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
        assertEquals(request.getId(), actualRequest.getId());
        assertEquals(request.getTransaction(), actualRequest.getTransaction());
    }

    @Test
    void givenEmptyMapAndBadToken_tryAddTransaction() {
        transactionId = random.nextLong();

        final var request = new TransactionPutRequest(transactionId, 1, TransactionPutRequest.Action.RETURN);
        assertFalse(transactionMgr.addTransactionPlan(request));
    }

    @Test
    void givenNonEmptyMapAndBadToken_tryAddTransaction() {
        transactionMgr.newTransaction();
        transactionMgr.newTransaction();
        transactionMgr.newTransaction();

        transactionId = random.nextLong();

        final var request = new TransactionPutRequest(transactionId, 1, TransactionPutRequest.Action.RETURN);
        assertFalse(transactionMgr.addTransactionPlan(request));
    }

    @Test
    void givenTokenExistsAndPlanNotEmpty_tryCommitTransactionPlan() {
        final var transaction = transactionMgr.newTransaction();

        final var putRequest = new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.RETURN);
        assumeTrue(transactionMgr.addTransactionPlan(putRequest));

        final var postRequest = new TransactionPostRequest(transaction, TransactionPostRequest.Operation.COMMIT);
        final var transactionPlan = transactionMgr.getAndEraseTransaction(postRequest);
        assertNotNull(transactionPlan);
        assertFalse(transactionPlan.isEmpty());
        assertEquals(1, transactionPlan.size());

        final var actualRequest = transactionPlan.get(0);
        assertEquals(putRequest.getTransaction(), actualRequest.getTransaction());
        assertEquals(putRequest.getId(), actualRequest.getId());
        assertEquals(putRequest.getTransaction(), actualRequest.getTransaction());
    }

    @Test
    void givenTokenExistsAndPlanNotEmpty_tryCancelTransactionPlan() {
        final var transaction = transactionMgr.newTransaction();

        final var putRequest = new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.RETURN);
        assumeTrue(transactionMgr.addTransactionPlan(putRequest));

        final var postRequest = new TransactionPostRequest(transaction, TransactionPostRequest.Operation.CANCEL);
        final var transactionPlan = transactionMgr.getAndEraseTransaction(postRequest);
        assertNotNull(transactionPlan);
        assertFalse(transactionPlan.isEmpty());
        assertEquals(1, transactionPlan.size());

        final var actualRequest = transactionPlan.get(0);
        assertEquals(putRequest.getTransaction(), actualRequest.getTransaction());
        assertEquals(putRequest.getId(), actualRequest.getId());
        assertEquals(putRequest.getTransaction(), actualRequest.getTransaction());
    }

    @Test
    void givenTokenNotExists_tryPopTransactionPlan() {
        transactionId = (long) random.nextInt(Integer.MAX_VALUE);
        final var postRequest = new TransactionPostRequest(transactionId, TransactionPostRequest.Operation.COMMIT);

        assertNull(transactionMgr.getAndEraseTransaction(postRequest));
    }

    @Test
    void givenTokenExistsAndPlanEmpty_tryPopTransactionPlan() {
        final var transaction = transactionMgr.newTransaction();

        final var postRequest = new TransactionPostRequest(transaction, TransactionPostRequest.Operation.COMMIT);
        final var transactionPlan = transactionMgr.getAndEraseTransaction(postRequest);
        assertNotNull(transactionPlan);
        assertTrue(transactionPlan.isEmpty());
    }

    @Test
    void givenMultiTokenExists_whenRemoveToken_oneTokenRemoved() {
        final var transaction = transactionMgr.newTransaction();
        transactionMgr.newTransaction();
        transactionMgr.newTransaction();

        final var putRequest = new TransactionPutRequest(transaction, 1, TransactionPutRequest.Action.RETURN);
        assumeTrue(transactionMgr.addTransactionPlan(putRequest));

        final var postRequest = new TransactionPostRequest(transaction, TransactionPostRequest.Operation.CANCEL);
        assertNotNull(transactionMgr.getAndEraseTransaction(postRequest));

        assertEquals(2, backingMap.size());
    }

    @AfterEach
    void tearDown() {
        transactionMgr = null;
        transactionListSupplier = null;
        backingMap = null;
        random = null;
    }
}
