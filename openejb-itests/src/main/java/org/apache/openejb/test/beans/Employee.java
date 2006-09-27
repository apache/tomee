package org.apache.openejb.test.beans;

import java.rmi.RemoteException;

public interface Employee extends javax.ejb.EJBObject {
    
    public void setFirstName(String firstName)
    throws RemoteException;
    
    public void setLastName(String lastName)
    throws RemoteException;
    
    public String getFirstName( )
    throws RemoteException;
    
    public String getLastName( )
    throws RemoteException;
    
}
    