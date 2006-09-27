package org.apache.openejb.test.beans;


public interface EmployeeHome extends javax.ejb.EJBHome{
    
    public Employee create(String lastname, String firstName)
    throws  javax.ejb.CreateException, java.rmi.RemoteException;
    
    public int sum(int one, int two) throws java.rmi.RemoteException;
    
    public java.util.Collection findAll( ) 
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public Employee findByPrimaryKey(Integer primkey)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
}