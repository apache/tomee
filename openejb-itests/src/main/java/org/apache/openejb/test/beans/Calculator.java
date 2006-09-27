package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

public interface Calculator extends javax.ejb.EJBObject {
    
    public int add(int a, int b) throws RemoteException;
    
    public int sub(int a, int b) throws RemoteException;
    
}