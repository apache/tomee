package org.apache.openejb.karaf.command;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.openejb.localclient.LocalInitialContextFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public abstract class JndiOsgiCommand extends OsgiCommandSupport {
    public <T> T lookup(final Class<T> clazz, final String jndiName) throws NamingException {
        Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            return (T) new InitialContext(p).lookup(jndiName);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
