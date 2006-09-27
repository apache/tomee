package org.apache.openejb.core.transaction;

import org.apache.openejb.core.ThreadContext;

public interface TransactionContainer {

    public void discardInstance(Object instance, ThreadContext context);

}

