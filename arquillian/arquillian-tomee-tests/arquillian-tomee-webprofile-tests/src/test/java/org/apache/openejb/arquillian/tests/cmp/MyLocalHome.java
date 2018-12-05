package org.apache.openejb.arquillian.tests.cmp;

public interface MyLocalHome extends javax.ejb.EJBLocalHome {

    public MyLocalObject createObject(String name)
            throws javax.ejb.CreateException;

    public MyLocalObject findByPrimaryKey(Integer primarykey)
            throws javax.ejb.FinderException;

    public java.util.Collection findEmptyCollection()
            throws javax.ejb.FinderException;

}
