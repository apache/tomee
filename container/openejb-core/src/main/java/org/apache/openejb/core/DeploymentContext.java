package org.apache.openejb.core;

import javax.naming.Context;

/**
 * @org.apache.xbean.XBean element="deploymentContext"
 */
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

    /**
     * @org.apache.xbean.Property alias="deploymentId"
     */
    public Object getId() {
        return id;
    }

    public Context getJndiContext() {
        return jndiContext;
    }
}
