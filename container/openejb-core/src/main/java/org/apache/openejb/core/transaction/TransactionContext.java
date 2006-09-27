package org.apache.openejb.core.transaction;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openejb.core.ThreadContext;

import java.util.Map;
import java.util.HashMap;

public class TransactionContext {

    public Transaction clientTx;
    public Transaction currentTx;
    public ThreadContext callContext;
    public final Map context = new HashMap();

    private final TransactionManager transactionManager;

    public TransactionContext(ThreadContext callContext, TransactionManager transactionManager) {
        this.callContext = callContext;
        this.transactionManager = transactionManager;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}

