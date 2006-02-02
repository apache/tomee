package org.openejb.test.entity.bmp;

import java.rmi.RemoteException;
import java.util.Properties;

import org.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface BasicBmp2DataSourcesObject extends javax.ejb.EJBObject{
    
    /**
     * Reverses the string passed in then returns it
     * 
     * @return 
     */
    public String businessMethod(String text) throws RemoteException;
    
    /**
     * Returns a report of the bean's 
     * runtime permissions
     * 
     * @return 
     */
    public Properties getPermissionsReport() throws RemoteException;
    
    /**
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName) throws RemoteException;
}
