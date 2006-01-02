package org.openejb.core.stateful;

import javax.ejb.EnterpriseBean;

import org.openejb.ApplicationException;
import org.openejb.InvalidateReferenceException;
import org.openejb.core.transaction.TransactionContext;
import org.openejb.core.transaction.TransactionPolicy;

public class StatefulContainerManagedTxPolicy extends org.openejb.core.transaction.TransactionPolicy {

    protected TransactionPolicy policy;

    public StatefulContainerManagedTxPolicy(TransactionPolicy policy) {
        this.policy = policy;
        this.container = policy.getContainer();
        this.policyType = policy.policyType;
        if (container instanceof org.openejb.Container &&
                ((org.openejb.Container) container).getContainerType() != org.openejb.Container.STATEFUL) {
            throw new IllegalArgumentException();
        }
    }

    public String policyToString() {
        return policy.policyToString();
    }

    public void beforeInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.SystemException, org.openejb.ApplicationException {
        policy.beforeInvoke(instance, context);
    }

    public void afterInvoke(EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        policy.afterInvoke(instance, context);
    }

    public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        policy.handleApplicationException(appException, context);
    }

    public void handleSystemException(Throwable sysException, EnterpriseBean instance, TransactionContext context) throws org.openejb.ApplicationException, org.openejb.SystemException {
        try {
            policy.handleSystemException(sysException, instance, context);
        } catch (ApplicationException e) {
            throw new InvalidateReferenceException(e.getRootCause());
        }
    }

}

