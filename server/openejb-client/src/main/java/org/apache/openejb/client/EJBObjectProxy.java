package org.apache.openejb.client;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.ejb.EJBObject;

public interface EJBObjectProxy extends Serializable, EJBObject {

    public EJBObjectHandler getEJBObjectHandler();

    public Object writeReplace() throws ObjectStreamException;

    public Object readResolve() throws ObjectStreamException;

}

