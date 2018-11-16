package org.apache.openejb.arquillian.tests.cmp;

import java.rmi.RemoteException;

public interface MyRemoteObject extends javax.ejb.EJBObject {

    public void doit() throws RemoteException;

}
