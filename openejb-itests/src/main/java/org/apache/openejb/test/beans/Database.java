package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

public interface Database extends javax.ejb.EJBObject {
    
    public void executeQuery(String statement) throws RemoteException, java.sql.SQLException;
    public boolean execute(String statement) throws RemoteException, java.sql.SQLException;
    
}