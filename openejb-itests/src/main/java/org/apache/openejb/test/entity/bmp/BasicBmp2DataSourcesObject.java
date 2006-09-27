package org.apache.openejb.test.entity.bmp;

import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BasicBmp2DataSourcesObject extends javax.ejb.EJBObject{
    
    /**
     * Reverses the string passed in then returns it
     */
    public String businessMethod(String text) throws RemoteException;
    
    /**
     * Returns a report of the bean's 
     * runtime permissions
     */
    public Properties getPermissionsReport() throws RemoteException;
    
    /**
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName) throws RemoteException;
}
