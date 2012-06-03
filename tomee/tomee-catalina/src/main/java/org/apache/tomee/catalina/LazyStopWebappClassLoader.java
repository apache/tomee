package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    public LazyStopWebappClassLoader() {
        // no-op
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
