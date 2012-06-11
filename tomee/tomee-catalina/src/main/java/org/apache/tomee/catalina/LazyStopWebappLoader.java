package org.apache.tomee.catalina;

import org.apache.catalina.loader.WebappLoader;

public class LazyStopWebappLoader extends WebappLoader {
    public LazyStopWebappLoader(final ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    public LazyStopWebappLoader() {
        // no-op
    }

    @Override
    public void backgroundProcess() {
        final ClassLoader classloader = super.getClassLoader();
        if (classloader instanceof LazyStopWebappClassLoader) {
            final LazyStopWebappClassLoader lazyStopWebappClassLoader = (LazyStopWebappClassLoader) classloader;
            lazyStopWebappClassLoader.restarting();
            try {
                super.backgroundProcess();
            } finally {
                lazyStopWebappClassLoader.restarted();
            }
        } else {
            super.backgroundProcess();
        }
    }
}
