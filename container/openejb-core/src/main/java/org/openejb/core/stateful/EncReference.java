package org.openejb.core.stateful;

import org.openejb.core.Operations;
import org.openejb.core.ivm.naming.Reference;

import javax.naming.NameNotFoundException;

public class EncReference extends org.openejb.core.ivm.naming.ENCReference {

    public EncReference(Reference ref) {
        super(ref);
    }

    public void checkOperation(byte operation) throws NameNotFoundException {

        if (operation == Operations.OP_AFTER_COMPLETION) {
            throw new NameNotFoundException("Operation Not Allowed");
        }

    }

}
