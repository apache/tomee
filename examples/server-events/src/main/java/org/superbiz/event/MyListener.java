package org.superbiz.event;

import org.apache.openejb.observer.Observes;

import java.util.logging.Logger;

/**
 * registered as service in openejb.xml, tomee.xml, resources.xml or openejb system properties:
 *
 * listener = new://Service?type=org.superbiz.event.MyListener
 * listener.logAllEvent = true
 */
public class MyListener {
    private static final Logger LOGGER = Logger.getLogger(MyListener.class.getName());

    private boolean logAllEvent = false;

    public void global(@Observes final Object event) {
        LOGGER.info(">>> an event occured -> " + event.toString());
    }

    // configurable
    public void setLogAllEvent(boolean logAllEvent) {
        this.logAllEvent = logAllEvent;
    }
}
