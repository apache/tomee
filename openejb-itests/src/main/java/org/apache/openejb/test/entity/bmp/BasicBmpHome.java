package org.apache.openejb.test.entity.bmp;

import java.util.Enumeration;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BasicBmpHome extends javax.ejb.EJBHome {

    public BasicBmpObject create(String name)
    throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    public BasicBmpObject findByPrimaryKey(Integer primarykey)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public java.util.Collection findEmptyCollection()
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public java.util.Collection findByLastName(String lastName)
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public Enumeration findEmptyEnumeration()
    throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public int sum(int x, int y) throws java.rmi.RemoteException;
}
