package org.apache.openejb.spi;

public interface SecurityService extends Service {

    public boolean isCallerAuthorized(Object securityIdentity, String [] roleNames);

    public Object translateTo(Object securityIdentity, Class type);

    /*
     * Associates a security identity object with the current thread. Setting 
     * this argument to null, will effectively dissociate the thread with a
     * security identity.  This is used when access enterprise beans through 
     * the global JNDI name space. Its not used when calling invoke on a 
     * RpcContainer object.
    */
    public void setSecurityIdentity(Object securityIdentity);

    /*
    * Obtains the security identity associated with the current thread.
    * If there is no association, then null is returned. 
    */
    public Object getSecurityIdentity();
}