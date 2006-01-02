package org.openejb.core;

import javax.naming.Context;

public class DeploymentContext {
    private final ClassLoader classLoader;
    private final Object id;
    private final Context jndiContext;

    public DeploymentContext(Object id, ClassLoader classLoader, Context jndiContext) {
        this.classLoader = classLoader;
        this.id = id;
        this.jndiContext = jndiContext;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Object getId() {
        return id;
    }

    public Context getJndiContext() {
        return jndiContext;
    }
}
