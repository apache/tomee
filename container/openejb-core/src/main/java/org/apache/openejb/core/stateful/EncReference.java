package org.apache.openejb.core.stateful;

import org.apache.openejb.core.Operations;
import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.NameNotFoundException;

public class EncReference extends org.apache.openejb.core.ivm.naming.ENCReference {

    public EncReference(Reference ref) {
        super(ref);
    }

    public void checkOperation(byte operation) throws NameNotFoundException {

        if (operation == Operations.OP_AFTER_COMPLETION) {
            throw new NameNotFoundException("Operation Not Allowed");
        }

    }

}
