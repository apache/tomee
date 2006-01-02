package org.openejb.core.stateful;

import java.util.Hashtable;
import java.util.Properties;

public interface PassivationStrategy {

    public void init(Properties props) throws org.openejb.SystemException;

    public void passivate(Hashtable stateTable)
            throws org.openejb.SystemException;

    public Object activate(Object primaryKey)
            throws org.openejb.SystemException;

}
