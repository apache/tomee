/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb.sfsb;

import javax.ejb.SessionBean;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.AbstractInstanceContext;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.util.TransactionUtils;

/**
 * @version $Revision$ $Date$
 */
public class StatefulInstanceContext extends AbstractInstanceContext {
    private static final Log log = LogFactory.getLog(StatefulInstanceContext.class);
    private final StatefulEjbContainer statefulEjbContainer;
    private final Object id;
    private final StatefulSessionContext statefulContext;
    private Transaction preexistingTransaction;
    private EJBOperation operation;
    private InstanceCache cache;

    public StatefulInstanceContext(StatefulEjbDeployment statefulEjbDeployment,
            StatefulEjbContainer statefulEjbContainer,
            SessionBean instance,
            Object id,
            EJBProxyFactory proxyFactory) {
        super(statefulEjbDeployment, instance, proxyFactory);

        this.statefulEjbContainer = statefulEjbContainer;
        this.id = id;

        TransactionManager transactionManager = statefulEjbContainer.getTransactionManager();

        UserTransaction userTransaction;
        if (statefulEjbDeployment.isBeanManagedTransactions()) {
            userTransaction = statefulEjbContainer.getUserTransaction();
        } else {
            userTransaction = null;
        }

        statefulContext = new StatefulSessionContext(this, transactionManager, userTransaction);
    }

    public EJBOperation getOperation() {
        return operation;
    }

    public void setOperation(EJBOperation operation) {
        statefulContext.setState(operation);
        this.operation = operation;
    }

    public boolean setTimerState(EJBOperation operation) {
        return statefulContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return statefulContext;
    }

    public Object getId() {
        return id;
    }

    public Transaction getPreexistingTransaction() {
        return preexistingTransaction;
    }

    public void setPreexistingTransaction(Transaction preexistingTransaction) {
        this.preexistingTransaction = preexistingTransaction;
    }

    public InstanceCache getCache() {
        return cache;
    }

    public void setCache(InstanceCache cache) {
        this.cache = cache;
    }

    public void die() {
        if (preexistingTransaction != null) {
            if (TransactionUtils.isActive(preexistingTransaction)) {
                try {
                    preexistingTransaction.rollback();
                } catch (Exception e) {
                    log.warn("Unable to roll back", e);
                }
            }
            preexistingTransaction = null;
        }
        if (cache != null) {
            cache.remove(id);
            cache = null;
        }
        super.die();
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        statefulEjbContainer.setContext(this, statefulContext);
    }

    public void associate() throws Throwable {
        super.associate();
        statefulEjbContainer.afterBegin(this);
    }

    public void beforeCommit() throws Throwable {
        super.beforeCommit();
        statefulEjbContainer.beforeCommit(this);
    }

    public void afterCommit(boolean committed) throws Throwable {
        super.afterCommit(committed);
        statefulEjbContainer.afterCommit(this, committed);
    }

    public void unassociate() throws Throwable {
        super.unassociate();
        if (!isDead() && cache != null) {
            cache.putInactive(id, this);
        }
    }
}
