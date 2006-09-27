package org.apache.openejb.test.stateless;



/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface ContainerTxStatelessHome extends javax.ejb.EJBHome {

    public ContainerTxStatelessObject create()
    throws javax.ejb.CreateException, java.rmi.RemoteException;
}
