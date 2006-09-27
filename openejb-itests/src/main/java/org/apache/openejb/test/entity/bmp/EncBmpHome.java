package org.apache.openejb.test.entity.bmp;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface EncBmpHome extends javax.ejb.EJBHome {


    public EncBmpObject create(String name)
    throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    public EncBmpObject findByPrimaryKey(Integer primarykey)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public java.util.Collection findEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
}
