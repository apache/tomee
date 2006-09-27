package org.apache.openejb.client;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.ejb.EJBHome;

public interface EJBHomeProxy extends Serializable, EJBHome {

    public EJBHomeHandler getEJBHomeHandler();

    public Object writeReplace() throws ObjectStreamException;

    public Object readResolve() throws ObjectStreamException;

}

