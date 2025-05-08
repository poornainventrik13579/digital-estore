package com.inventrik.digitalestore.service.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Service responsible for coordinating transactions across multiple entities
 * to ensure data consistency, especially for payment operations.
 */
@Service
public class TransactionCoordinatorService {

    /**
     * Execute an operation within a transaction with default settings.
     * Uses REQUIRED propagation and READ_COMMITTED isolation.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public <T> T executeInTransaction(Supplier<T> operation) {
        return operation.get();
    }

    /**
     * Execute an operation within a transaction with strict isolation.
     * Uses REQUIRED propagation and SERIALIZABLE isolation.
     * This is appropriate for critical financial operations.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public <T> T executeInStrictTransaction(Supplier<T> operation) {
        return operation.get();
    }

    /**
     * Execute an operation that creates a new transaction regardless
     * of an existing transaction context.
     * This is useful for operations that should not be affected by
     * a rollback in the calling transaction.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public <T> T executeInNewTransaction(Supplier<T> operation) {
        return operation.get();
    }
    
    /**
     * Execute an operation that should always be committed,
     * even if the caller rolls back. Useful for audit logging.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeWithIndependentCommit(Supplier<T> operation) {
        return operation.get();
    }
}