package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;

public interface ShoppingCartHome extends EJBHome {
    
    public ShoppingCart create(String name) throws RemoteException;
    
   
}
