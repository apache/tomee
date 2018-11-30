package org.apache.openejb.arquillian.tests.cmp;

public interface MyRemoteHome extends javax.ejb.EJBHome {

    public MyRemoteObject createObject(String name)
            throws javax.ejb.CreateException, java.rmi.RemoteException;

    public MyRemoteObject findByPrimaryKey(Integer primarykey)
            throws javax.ejb.FinderException, java.rmi.RemoteException;

    public java.util.Collection findEmptyCollection()
            throws javax.ejb.FinderException, java.rmi.RemoteException;

}
