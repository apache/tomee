package org.openejb.core.stateless;

import org.openejb.core.ivm.naming.Reference;

import javax.naming.NameNotFoundException;

public class EncReference extends org.openejb.core.ivm.naming.ENCReference {

    public EncReference(Reference ref) {
        super(ref);
    }

    public void checkOperation(byte operation) throws NameNotFoundException {

/*
        if( operation != Operations.OP_BUSINESS ){
            throw new NameNotFoundException("Operation Not Allowed");
        }        
*/
    }

}
