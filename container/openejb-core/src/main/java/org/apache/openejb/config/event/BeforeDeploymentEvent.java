package org.apache.openejb.config.event;

import org.apache.openejb.observer.Event;

import java.net.URL;
import java.util.Arrays;

@Event
public class BeforeDeploymentEvent {
    private final URL[] urls;

    public BeforeDeploymentEvent(final URL[] files) {
        urls = files;
    }

    public URL[] getUrls() {
        return urls;
    }

    @Override
    public String toString() {
        return "BeforeDeploymentEvent{" +
                "urls=" + Arrays.asList(urls) +
            '}';
    }
}
