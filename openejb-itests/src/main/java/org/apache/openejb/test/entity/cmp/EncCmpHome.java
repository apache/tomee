package org.apache.openejb.test.entity.cmp;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface EncCmpHome extends javax.ejb.EJBHome {


    public EncCmpObject create(String name)
    throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    public EncCmpObject findByPrimaryKey(Integer primarykey)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public java.util.Collection findEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
}
