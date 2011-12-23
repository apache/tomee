package org.apache.openejb.client;

import org.apache.openejb.localclient.LocalInitialContextFactory;

import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @deprecated use org.apache.openejb.localclient.LocalInitialContext
 */
public class LocalInitialContext extends org.apache.openejb.localclient.LocalInitialContext {
    public LocalInitialContext(Hashtable env, LocalInitialContextFactory factory) throws NamingException {
        super(env, factory);
    }
}
