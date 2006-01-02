package org.openejb.core.transaction;

import javax.transaction.Transaction;

import org.openejb.core.ThreadContext;

public class TransactionContext {

    public Transaction clientTx;
    public Transaction currentTx;
    public ThreadContext callContext;

    public TransactionContext() {
    }

    public TransactionContext(ThreadContext callContext) {
        this.callContext = callContext;
    }
}

