package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.openejb.loader.SystemInstance;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";

    public LazyStopWebappClassLoader() {
        setDelegate(!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true));
    }

    public LazyStopWebappClassLoader(final ClassLoader parent) {
        super(parent);
    }

    @Override
    public void stop() throws LifecycleException {
        // no-op: in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
    }

    public void internalStop() throws LifecycleException {
        super.stop();
    }
}
