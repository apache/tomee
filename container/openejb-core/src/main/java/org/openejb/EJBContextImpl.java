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
package org.openejb;

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.TimerService;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.security.auth.Subject;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;

/**
 * Implementation of EJBContext that uses the State pattern to determine
 * which operations can be performed for a given EJB.
 *
 * @version $Revision$ $Date$
 */
public abstract class EJBContextImpl {
    protected final EJBInstanceContext context;
    protected final UserTransactionImpl userTransaction;
    private final TransactionContextManager transactionContextManager;
    private Subject callerSubject;
    protected EJBContextState state;

    public EJBContextImpl(EJBInstanceContext context, TransactionContextManager transactionContextManager, UserTransactionImpl userTransaction) {
        this.context = context;
        this.userTransaction = userTransaction;
        this.transactionContextManager = transactionContextManager;
    }

    public Subject getCallerSubject() {
        return callerSubject;
    }

    public void setCallerSubject(Subject callerSubject) {
        this.callerSubject = callerSubject;
    }

    public EJBHome getEJBHome() {
        return state.getEJBHome(context);
    }

    public EJBLocalHome getEJBLocalHome() {
        return state.getEJBLocalHome(context);
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        return state.getEJBObject(context);
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        return state.getEJBLocalObject(context);
    }

    public Principal getCallerPrincipal() {
        return state.getCallerPrincipal(callerSubject);
    }

    public boolean isCallerInRole(String s) {
        return state.isCallerInRole(s, context);
    }

    public UserTransaction getUserTransaction() {
        // handle the getUserTransaction directly as it is anoying and always allowed
        if (userTransaction == null) {
            throw new IllegalStateException("getUserTransaction is not allowed when using Container Managed Transactions");
        }
        return state.getUserTransaction(userTransaction);
    }

    public void setRollbackOnly() {
        if (userTransaction != null) {
            throw new IllegalStateException("Calls to setRollbackOnly are not allowed for EJBs with bean-managed transaction demarcation");
        }
        state.setRollbackOnly(context, transactionContextManager);
    }

    public boolean getRollbackOnly() {
        if (userTransaction != null) {
            throw new IllegalStateException("Calls to getRollbackOnly are not allowed for EJBs with bean-managed transaction demarcation");
        }
        return state.getRollbackOnly(context, transactionContextManager);
    }

    public TimerService getTimerService() {
        return state.getTimerService(context);
    }

    public Properties getEnvironment() {
        throw new EJBException("getEnvironment is no longer supported; use JNDI instead");
    }

    public Identity getCallerIdentity() {
        throw new EJBException("getCallerIdentity is no longer supported; use getCallerPrincipal instead");
    }

    public boolean isCallerInRole(Identity identity) {
        throw new EJBException("isCallerInRole(Identity role) is no longer supported; use isCallerInRole(String roleName) instead");
    }

    public abstract static class EJBContextState {
        public EJBHome getEJBHome(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBHome home = context.getProxyFactory().getEJBHome();
                return home;
            }
            throw new IllegalStateException("getEJBHome is not allowed if no home interface is defined");
        }

        public EJBLocalHome getEJBLocalHome(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBLocalHome localHome = context.getProxyFactory().getEJBLocalHome();
                return localHome;
            }
            throw new IllegalStateException("getEJBLocalHome is not allowed if no local localHome interface is defined");
        }

        public EJBObject getEJBObject(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBObject remote = context.getProxyFactory().getEJBObject(context.getId());
                return remote;
            }
            throw new IllegalStateException("getEJBObject is not allowed if no remote interface is defined");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                EJBLocalObject local = context.getProxyFactory().getEJBLocalObject(context.getId());
                return local;
            }
            throw new IllegalStateException("getEJBLocalObject is not allowed if no local interface is defined");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            return ContextManager.getCurrentPrincipal(callerSubject);
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            if( context.getProxyFactory() !=null ) {
                return ContextManager.isCallerInRole(context.getProxyFactory().getEJBName(), s);
            }
            throw new IllegalStateException("isCallerInRole is not allowed if no local or remote interface is defined");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            return userTransaction;
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            TransactionContext ctx = transactionContextManager.getContext();
            if (ctx == null || !ctx.isInheritable() || !ctx.isActive()) {
                throw new IllegalStateException("There is no transaction in progess.");
            }
            try {
                ctx.setRollbackOnly();
            } catch (SystemException e) {
                throw new EJBException(e);
            }
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            TransactionContext ctx = transactionContextManager.getContext();
            if (ctx == null || !ctx.isInheritable() || !ctx.isActive()) {
                throw new IllegalStateException("There is no transaction in progess.");
            }
            try {
                return ctx.getRollbackOnly();
            } catch (SystemException e) {
                throw new EJBException(e);
            }
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            TimerService timerService = context.getTimerService();
            if (timerService == null) {
                //TODO is this correct?
                throw new IllegalStateException("EJB does not implement EJBTimeout");
            }
            return timerService;
        }
    }
}
