package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;
/*
  This Reference type is used only by the Castor JDO CMP 1.1 container.
  It allows the TransactionManager to be discovered at runtime, which is 
  needed because its not yet available when the container is being constructed
  and the Reference is being bound to the JNDI name space of the deployment.
  See the init( ) method of the CastorCMP11_EntityContainer. 
*/

public class JndiTxReference implements Reference {

    private final TransactionManager transactionManager;

    public JndiTxReference(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object getObject() throws NamingException {
        return transactionManager;
    }

}