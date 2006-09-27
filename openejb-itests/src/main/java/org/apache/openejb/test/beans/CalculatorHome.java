package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

public interface CalculatorHome extends javax.ejb.EJBHome {
    
    public Calculator create( ) throws RemoteException;
    
}