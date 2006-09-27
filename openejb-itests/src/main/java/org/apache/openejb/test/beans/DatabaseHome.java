package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

public interface DatabaseHome extends javax.ejb.EJBHome {
    
    public Database create( ) throws javax.ejb.CreateException, RemoteException;
    
}