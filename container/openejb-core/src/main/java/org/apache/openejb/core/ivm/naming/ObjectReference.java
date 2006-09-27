package org.apache.openejb.core.ivm.naming;

import javax.naming.NamingException;

public class ObjectReference implements Reference {

    private Object obj;

    public ObjectReference(Object obj) {
        this.obj = obj;
    }

    public Object getObject() throws NamingException {
        return obj;
    }
}
