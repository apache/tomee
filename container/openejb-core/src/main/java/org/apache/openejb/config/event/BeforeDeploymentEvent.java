package org.apache.openejb.config.event;

import org.apache.openejb.observer.Event;

import java.net.URL;
import java.util.Arrays;

@Event
public class BeforeDeploymentEvent {
    private final URL[] urls;
    private final ClassLoader parentClassLoader;

    public BeforeDeploymentEvent(final URL[] files) {
        this(files, null);
    }

    public BeforeDeploymentEvent(final URL[] files, final ClassLoader parent) {
        urls = files;
        parentClassLoader = parent;
    }

    public URL[] getUrls() {
        return urls;
    }

    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        return getClass().getClassLoader();
    }

    @Override
    public String toString() {
        return "BeforeDeploymentEvent{" +
                "urls=" + Arrays.asList(urls) +
            '}';
    }
}
