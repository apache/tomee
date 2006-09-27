package org.apache.openejb.core.entity;

import javax.naming.NameNotFoundException;

import org.apache.openejb.core.ivm.naming.Reference;

/*
  This class is a wrapper for an Intra-VM EJB or Connector references in the 
  JNDI ENC of a entity bean.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.
*/

public class EncReference extends org.apache.openejb.core.ivm.naming.ENCReference {

    public EncReference(Reference ref) {
        super(ref);
    }

    /*
    * This method is invoked by the ENCReference super class each time its 
    * getObject() method is called within the container system.  This checkOperation
    * method ensures that the entity bean is in the correct state before the super
    * class can return the requested reference object.
    */
    public void checkOperation(byte operation) throws NameNotFoundException {

        /*        if( operation == Operations.OP_SET_CONTEXT || 
            operation == Operations.OP_UNSET_CONTEXT || 
            operation == Operations.OP_PASSIVATE ||
            operation == Operations.OP_ACTIVATE ){
                throw new NameNotFoundException("Operation Not Allowed");
        }
*/
    }

}
