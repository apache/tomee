package org.apache.openejb.client;

import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @deprecated use org.apache.openejb.core.LocalInitialContext
 */
public class LocalInitialContext extends org.apache.openejb.core.LocalInitialContext {
    public LocalInitialContext(Hashtable env, org.apache.openejb.core.LocalInitialContextFactory factory) throws NamingException {
        super(env, factory);
    }
}
