package org.openejb.alt.containers.castor_cmp11;

import javax.naming.NamingException;

import org.openejb.core.ivm.naming.Reference;
/*
  This Reference type is used only by the Castor JDO CMP 1.1 container.
  It allows the TransactionManager to be discovered at runtime, which is 
  needed because its not yet available when the container is being constructed
  and the Reference is being bound to the JNDI name space of the deployment.
  See the init( ) method of the CastorCMP11_EntityContainer. 
*/

public class JndiTxReference implements Reference {

    javax.transaction.TransactionManager txMngr;

    public Object getObject() throws NamingException {
        if (txMngr == null)
            txMngr = org.openejb.OpenEJB.getTransactionManager();
        return txMngr;
    }

}