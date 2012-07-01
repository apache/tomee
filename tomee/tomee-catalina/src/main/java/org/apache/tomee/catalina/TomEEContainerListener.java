package org.apache.tomee.catalina;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.core.StandardContext;

public class TomEEContainerListener implements ContainerListener {
    private static final ThreadLocal<StandardContext> context = new ThreadLocal<StandardContext>();

    @Override
    public void containerEvent(final ContainerEvent event) {
        if ("beforeContextInitialized".equals(event.getType())) {
            context.set((StandardContext) event.getContainer());
        } else if ("afterContextInitialized".equals(event.getType())) {
            context.remove();
        }
    }

    public static StandardContext get() {
        return context.get();
    }
}
