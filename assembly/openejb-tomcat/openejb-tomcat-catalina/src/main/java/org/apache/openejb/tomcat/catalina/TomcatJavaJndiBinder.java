package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;

/**
 * @author rmannibucau
 */
public class TomcatJavaJndiBinder implements LifecycleListener {
    @Override public void lifecycleEvent(LifecycleEvent event) {
        Object source = event.getSource();
        if (source instanceof StandardContext) {
            StandardContext context = (StandardContext) source;
            if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
                TomcatJndiBuilder.mergeJava(context);
            }
        }
    }
}
