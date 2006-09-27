package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;

import javax.transaction.RollbackException;

import org.apache.openejb.test.object.Account;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface ContainerTxStatelessObject extends javax.ejb.EJBObject{
    
    public String txMandatoryMethod(String message) throws RemoteException;
    
    public String txNeverMethod(String message) throws RemoteException;
    
    public String txNotSupportedMethod(String message) throws RemoteException;
    
    public String txRequiredMethod(String message) throws RemoteException;
    
    public String txRequiresNewMethod(String message) throws RemoteException;
    
    public String txSupportsMethod(String message) throws RemoteException;

    public Account retreiveAccount(String ssn) throws RemoteException;
    
    public void openAccount(Account acct, Boolean rollback) throws RemoteException, RollbackException;
}
