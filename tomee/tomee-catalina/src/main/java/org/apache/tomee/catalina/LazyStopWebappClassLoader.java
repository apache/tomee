package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.openejb.loader.SystemInstance;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";

    private boolean restarting;

    public LazyStopWebappClassLoader() {
        setDelegate(!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true));
    }

    public LazyStopWebappClassLoader(final ClassLoader parent) {
        super(parent);
    }

    @Override
    public void stop() throws LifecycleException {
        // in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
        // exception: restarting we really stop it for the moment
        if (restarting) {
            internalStop();
        }
    }

    public void internalStop() throws LifecycleException {
        super.stop();
    }

    public void restarting() {
        restarting = true;
    }

    public void restarted() {
        restarting = false;
    }
}
