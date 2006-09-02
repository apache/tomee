package org.openejb.test.stateless;


/**
 * 
 * @author <a href="mailto:nour.mohammad@gmail.com">Mohammad Nour El-Din</a>
 */
public interface BasicStatelessLocalHome extends javax.ejb.EJBLocalHome {

    public BasicStatelessLocalObject create() throws javax.ejb.CreateException;
}
