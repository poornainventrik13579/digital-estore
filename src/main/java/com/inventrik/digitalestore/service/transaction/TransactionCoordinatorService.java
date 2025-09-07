package com.inventrik.digitalestore.service.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionCoordinatorService {

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public <T> T executeInTransaction(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public <T> T executeInStrictTransaction(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public <T> T executeInNewTransaction(Supplier<T> operation) {
        return operation.get();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeWithIndependentCommit(Supplier<T> operation) {
        return operation.get();
    }
}