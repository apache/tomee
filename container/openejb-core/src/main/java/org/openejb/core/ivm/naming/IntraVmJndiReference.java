package org.openejb.core.ivm.naming;

import javax.naming.NamingException;

import org.openejb.OpenEJB;

public class IntraVmJndiReference implements Reference{

    private String    jndiName;

    public IntraVmJndiReference(String jndiName){
        this.jndiName = jndiName;
    }

    public Object getObject( ) throws NamingException{
        return OpenEJB.getJNDIContext().lookup( jndiName );
    }
}
