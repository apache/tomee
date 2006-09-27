package org.apache.openejb.test.entity.bmp;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BasicBmp2DataSourcesHome extends javax.ejb.EJBHome {


    public BasicBmp2DataSourcesObject create(String name)
    throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    public BasicBmp2DataSourcesObject findByPrimaryKey(Integer primarykey)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public java.util.Collection findEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public int sum(int x, int y) throws java.rmi.RemoteException;
}
