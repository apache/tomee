package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BasicStatelessBean implements javax.ejb.SessionBean{
    
    private String name;
    private SessionContext ejbContext;
    private Hashtable allowedOperationsTable = new Hashtable();
    
    
    //=============================
    // Home interface methods
    //    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    /**
     * Maps to BasicStatelessObject.businessMethod
     * 
     * @return 
     * @see BasicStatelessObject#businessMethod
     */
    public String businessMethod(String text){
        testAllowedOperations("businessMethod");
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }
    

    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws ApplicationException{
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }
    
    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the 
     * destruction of the instance and invalidation of the
     * remote reference.
     * 
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }
    
    /**
     * Maps to BasicStatelessObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return 
     * @see BasicStatelessObject#getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicStatelessObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     * @see BasicStatelessObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }

    //    
    // Remote interface methods
    //=============================


    //================================
    // SessionBean interface methods
    //    
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setSessionContext");
    }
    /**
     * 
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
        this.name = "nameless automaton";
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
        testAllowedOperations("ejbRemove");
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
        // Should never called.
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbPassivate");
        // Should never called.
    }

    //    
    // SessionBean interface methods
    //================================
    
	protected void testAllowedOperations(String methodName) {
		OperationsPolicy policy = new OperationsPolicy();
	
		/*[0] Test getEJBHome /////////////////*/ 
		try {
			ejbContext.getEJBHome();
			policy.allow(policy.Context_getEJBHome);
		} catch (IllegalStateException ise) {
		}
	
		/*[1] Test getCallerPrincipal /////////*/
		try {
			ejbContext.getCallerPrincipal();
			policy.allow( policy.Context_getCallerPrincipal );
		} catch (IllegalStateException ise) {
		}
	
		/*[2] Test isCallerInRole /////////////*/
		try {
			ejbContext.isCallerInRole("ROLE");
			policy.allow( policy.Context_isCallerInRole );
		} catch (IllegalStateException ise) {
		}
	
		/*[3] Test getRollbackOnly ////////////*/
		try {
			ejbContext.getRollbackOnly();
			policy.allow( policy.Context_getRollbackOnly );
		} catch (IllegalStateException ise) { 
		}
	
		/*[4] Test setRollbackOnly ////////////*/
		try {
			ejbContext.setRollbackOnly();
			policy.allow( policy.Context_setRollbackOnly );
		} catch (IllegalStateException ise) {
		}
	
		/*[5] Test getUserTransaction /////////*/
		try {
			ejbContext.getUserTransaction();
			policy.allow( policy.Context_getUserTransaction );
		} catch (IllegalStateException ise) {
		}
	
		/*[6] Test getEJBObject ///////////////*/
		try {
			ejbContext.getEJBObject();
			policy.allow( policy.Context_getEJBObject );
		} catch (IllegalStateException ise) {
		}
	
		/*[7] Test Context_getPrimaryKey ///////////////
		 *
		 * Can't really do this
		 */
	
		/*[8] Test JNDI_access_to_java_comp_env ///////////////*/
		try {
			InitialContext jndiContext = new InitialContext();            
	
			String actual = (String)jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");
	
			policy.allow( policy.JNDI_access_to_java_comp_env );
		} catch (IllegalStateException ise) {
		} catch (javax.naming.NamingException ne) {
		}
	
		/*[9] Test Resource_manager_access ///////////////*/
		try {
			InitialContext jndiContext = new InitialContext( ); 
	
			DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/stateless/references/Resource_manager_access");
	
			policy.allow( policy.Resource_manager_access );
		} catch (IllegalStateException ise) {
		} catch (javax.naming.NamingException ne) {
		}
	
		/*[10] Test Enterprise_bean_access ///////////////*/
		try {
			InitialContext jndiContext = new InitialContext( ); 
	
			Object obj = jndiContext.lookup("java:comp/env/stateless/beanReferences/Enterprise_bean_access");
	
			policy.allow( policy.Enterprise_bean_access );
		} catch (IllegalStateException ise) {
		} catch (javax.naming.NamingException ne) {
		}
	
		allowedOperationsTable.put(methodName, policy);
	}

}
