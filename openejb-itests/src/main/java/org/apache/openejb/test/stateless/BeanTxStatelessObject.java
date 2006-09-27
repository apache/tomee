package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;

import org.apache.openejb.test.object.Account;
import org.apache.openejb.test.object.Transaction;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BeanTxStatelessObject extends javax.ejb.EJBObject{
    
    public Transaction getUserTransaction() throws RemoteException;
    
    public Transaction jndiUserTransaction() throws RemoteException;

    public void openAccount(Account account, Boolean commit) throws RemoteException, javax.transaction.RollbackException;

    public Account retreiveAccount(String ssn) throws RemoteException;
}
