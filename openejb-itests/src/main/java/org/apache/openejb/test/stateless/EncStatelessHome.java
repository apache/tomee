package org.apache.openejb.test.stateless;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface EncStatelessHome extends javax.ejb.EJBHome {

    public EncStatelessObject create()
    throws javax.ejb.CreateException, java.rmi.RemoteException;
}
