/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.observer.event;

import org.apache.openejb.observer.Event;

import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
@Event
public class ObserverFailed {

    private final Object observer;

    private final Method method;

    private final Object event;

    private final Throwable throwable;

    /**
     *
     * @param observer Object
     * @param method Method
     * @param event Object
     * @param throwable Throwable
     */
    public ObserverFailed(final Object observer, final Method method, final Object event, final Throwable throwable) {
        this.observer = observer;
        this.event = event;
        this.method = method;
        this.throwable = throwable;
    }

    /**
     *
     * @return Method
     */
    public Method getMethod() {
        return method;
    }

    /**
     *
     * @return Object
     */
    public Object getObserver() {
        return observer;
    }

    /**
     *
     * @return Object
     */
    public Object getEvent() {
        return event;
    }

    /**
     *
     * @return Throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "ObserverFailed{" +
            "observer=" + observer.getClass().getName() +
            ", method='" + method.toString() + "'" +
            ", throwable=" + throwable.getClass().getName() +
            "} " + event;
    }
}
