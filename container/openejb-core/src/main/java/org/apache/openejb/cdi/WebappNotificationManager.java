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
package org.apache.openejb.cdi;

import org.apache.webbeans.event.EventMetadataImpl;
import org.apache.webbeans.event.NotificationManager;

import jakarta.enterprise.inject.spi.ObserverMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NotificationManager which handles delegation to the parent NotificationManager
 */
public final class WebappNotificationManager extends NotificationManager {
    private final NotificationManager parentNotificationManager;

    public WebappNotificationManager(WebappWebBeansContext webappWebBeansContext) {
        super(webappWebBeansContext);
        this.parentNotificationManager = webappWebBeansContext.getParent() != null
                ? webappWebBeansContext.getParent().getNotificationManager()
                : null;
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
