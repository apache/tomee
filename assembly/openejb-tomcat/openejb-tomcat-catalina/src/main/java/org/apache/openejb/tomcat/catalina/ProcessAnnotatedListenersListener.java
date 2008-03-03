/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.ContainerListener;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.core.StandardContext;
import org.apache.openejb.tomcat.common.LegacyAnnotationProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * The StandardContext only calls these events for the listeners in the
 * getApplicationLifecycleListeners() list and not for any listeners in the
 * getApplicationEventListeners() list.  This is a big pain as we need to
 * do annotation processing for both.  The code gets a bit tricky as a result.
 *
 * Note this class only pertains to Tomcat 5.5.x and before, not 6.0.x and after.
 *
 * @version $Rev$ $Date$
 */
public class ProcessAnnotatedListenersListener extends LegacyAnnotationProcessorListener implements ContainerListener {
    private Object[] applicationLifecycleListeners;
    private Set<Object> destroyed;

    public ProcessAnnotatedListenersListener(LegacyAnnotationProcessor annotationProcessor) {
        super(annotationProcessor);
    }

    public void containerEvent(ContainerEvent event) {
        String type = event.getType();
        if ("beforeContextInitialized".equals(type)) {
            listenerStart(event);
        } else if ("afterContextDestroyed".equals(type)) {
            listenerStop(event);
        }
    }

    /**
     * We must process all the listeners on the very first "beforeContextInitialized" event of this Container
     * @param event
     */
    private void listenerStart(ContainerEvent event) {
        if (!isFirstBeforeContextInitializedEvent(event)) return;

        StandardContext standardContext = (StandardContext) event.getContainer();

        for (Object listener : getListeners(standardContext)) {
            processAnnotations(listener);
            postConstruct(listener);
        }
    }

    private boolean isFirstBeforeContextInitializedEvent(ContainerEvent event) {
        if (applicationLifecycleListeners != null) return false;

        StandardContext standardContext = (StandardContext) event.getContainer();
        applicationLifecycleListeners = standardContext.getApplicationLifecycleListeners();
        destroyed = new HashSet<Object>();
        return true;
    }

    /**
     * We must process all the listeners on the very last "afterContextDestroyed" event of this Container
     *
     * Looking at the code it's possible that if a ContainerListener threw an exception in the
     * afterContextDestroyed event that the afterContextDestroyed will get fired again in the catch block,
     * so we need to watch for that.  We use a set to ensure we can track uniqe events.
     * @param event
     */
    private void listenerStop(ContainerEvent event) {
        if (!isLastAfterContextDestroyedEvent(event)) return;

        StandardContext standardContext = (StandardContext) event.getContainer();

        for (Object listener : getListeners(standardContext)) {
            preDestroy(listener);
        }
    }

    private boolean isLastAfterContextDestroyedEvent(ContainerEvent event) {
        // Something very strange is going on if either of these are null at this stage
        if (destroyed == null || applicationLifecycleListeners == null) return false;

        // We've already been called and processed our last event
        if (destroyed.size() == applicationLifecycleListeners.length) return false;

        Object listener = event.getData();
        destroyed.add(listener);

        return (destroyed.size() == applicationLifecycleListeners.length);
    }

    private List<Object> getListeners(StandardContext standardContext) {
        ArrayList<Object> listeners = new ArrayList<Object>();
        listeners.addAll(Arrays.asList(standardContext.getApplicationEventListeners()));
        listeners.addAll(Arrays.asList(standardContext.getApplicationLifecycleListeners()));
        return listeners;
    }

}
