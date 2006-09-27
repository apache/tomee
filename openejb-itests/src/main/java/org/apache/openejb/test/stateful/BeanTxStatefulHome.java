package org.apache.openejb.test.stateful;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BeanTxStatefulHome extends javax.ejb.EJBHome {

    public BeanTxStatefulObject create(String name)
    throws javax.ejb.CreateException, java.rmi.RemoteException;
}
