package org.openejb.core.transaction;

import javax.ejb.EnterpriseBean;

import org.openejb.core.ThreadContext;

public interface TransactionContainer {

    public void discardInstance(EnterpriseBean instance, ThreadContext context);

}

