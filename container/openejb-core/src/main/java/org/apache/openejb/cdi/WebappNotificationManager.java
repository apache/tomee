package org.apache.openejb.cdi;

import org.apache.webbeans.event.EventMetadataImpl;
import org.apache.webbeans.event.NotificationManager;

import javax.enterprise.inject.spi.ObserverMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NotificationManager which handles delegation to the parent NotifcationManager
 */
public final class WebappNotificationManager extends NotificationManager {
    private final NotificationManager parentNotificationManager;

    /**
     * We need to know when we did start.
     * Lifecycle events will only get sent to the parent NotificationManager once the boot is finished.
     * This is necessary to e.g. handle ProcessInjectionPoint for manual InjectionPointFactory calls.
     */
    private boolean hasStarted = false;

    public WebappNotificationManager(WebappWebBeansContext webappWebBeansContext) {
        super(webappWebBeansContext);
        this.parentNotificationManager = webappWebBeansContext.getParent() != null
                ? webappWebBeansContext.getParent().getNotificationManager()
                : null;
    }


    @Override
    public void afterStart() {
        hasStarted = true;
        super.afterStart();
    }

    /**
     * Collect the observer methods of the parent BeanManager plus the own.
     */
    @Override
    public <T> Collection<ObserverMethod<? super T>> resolveObservers(T event, EventMetadataImpl metadata, boolean isLifecycleEvent) {
        if (isLifecycleEvent) {
            // we do not send lifecycle events to the parent beanmanager
            // because the same Extensions get loaded with different instances one per BeanManager anyway
            return super.resolveObservers(event, metadata, isLifecycleEvent);
        }

        // for standard event and some lifecycle events at RUNTIME(!),
        // we also have to invoke the parent NotificationManager
        List<ObserverMethod<? super T>> observerMethods =
                parentNotificationManager != null
                        ? new ArrayList<>(parentNotificationManager.resolveObservers(event, metadata, isLifecycleEvent))
                        : new ArrayList<>();
        observerMethods.addAll(super.resolveObservers(event, metadata, isLifecycleEvent));
        return observerMethods;
    }
}
