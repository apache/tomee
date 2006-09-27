package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface ShoppingCart extends EJBObject {
    
    public String getName( )throws RemoteException;
    
    public void setName(String name)throws RemoteException;
    
    public Calculator getCalculator() throws RemoteException;
    
    public void doJdbcCall() throws RemoteException;
    
}