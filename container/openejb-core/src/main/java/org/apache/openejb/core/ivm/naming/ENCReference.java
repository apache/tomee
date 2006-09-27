package org.apache.openejb.core.ivm.naming;

import javax.naming.NameNotFoundException;

import org.apache.openejb.core.ThreadContext;

/*
  This class is a wrapper for an Intra-VM EJB or Connector references in the 
  JNDI ENC of a entity, stateful and stateless beans.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.

  This class is subclassed by ENCReference in the entity, stateful and stateless packages 
  of org.apache.openejb.core.
*/

public abstract class ENCReference implements Reference {

    protected Reference ref = null;
    protected boolean checking = true;

    public ENCReference(Reference ref) {
        this.ref = ref;
    }

    public void setChecking(boolean value) {
        checking = value;
    }

    /*
    * Obtains the referenced object.
    */
    public Object getObject() throws javax.naming.NamingException {
        if (ThreadContext.isValid()) {
            ThreadContext cntx = ThreadContext.getThreadContext();
            byte operation = cntx.getCurrentOperation();
            checkOperation(operation);
        }
        return ref.getObject();
    }

    public abstract void checkOperation(byte opertionType) throws NameNotFoundException;
}
