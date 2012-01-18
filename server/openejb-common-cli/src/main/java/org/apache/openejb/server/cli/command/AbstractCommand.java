package org.apache.openejb.server.cli.command;

import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.server.cli.StreamManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public abstract class AbstractCommand {
    protected StreamManager streamManager;
    protected  String command;

    public abstract String name();
    public abstract void execute(final String cmd);
    public abstract String description();

    public String usage() {
        return name();
    }

    public void setStreamManager(StreamManager streamManager) {
        this.streamManager = streamManager;
    }

    public void setCommand(String command) {
        this.command = command;
    }

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
