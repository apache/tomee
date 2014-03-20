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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.observer;

import org.apache.openejb.observer.event.ObserverAdded;
import org.apache.openejb.observer.event.ObserverFailed;
import org.apache.openejb.observer.event.ObserverRemoved;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

public class ObserverManager {
    private final List<Observer> observers = new ArrayList<Observer>();

    public boolean addObserver(Object observer) {
        if (observer == null) throw new IllegalArgumentException("observer cannot be null");

        final Observer obs;
        try {
            obs = new Observer(observer);
        } catch (NotAnObserverException naoe) {
            return false;
        }

        final boolean added = observers.add(obs);
        if (added) {
            // Observers can observe they have been added and are active
            fireEvent(new ObserverAdded(observer));
        }
        return added;
    }

    public boolean removeObserver(Object observer) {
        if (observer == null) throw new IllegalArgumentException("observer cannot be null");

        final boolean removed = observers.remove(new Observer(observer));
        if (removed) {
            // Observers can observe they are to be removed
            fireEvent(new ObserverRemoved(observer));
        }
        return removed;
    }

    public void fireEvent(Object event) {
        if (event == null) throw new IllegalArgumentException("event cannot be null");

        final List<Invocation> invocations = new LinkedList<Invocation>();
        for (final Observer observer : observers) {
            final Invocation i = observer.toInvocation(event);
            if (i != null) {
                invocations.add(i);
            }
        }

        sort(invocations);
        for (final Invocation invocation : invocations) {
            try {
                invocation.proceed();
            } catch (final Throwable t) {
                if (!(event instanceof ObserverFailed)) {
                    fireEvent(new ObserverFailed(invocation.observer, event, t));
                }
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    Logger.getLogger(ObserverManager.class.getName()).log(Level.SEVERE, "error invoking " + invocation.observer, t.getCause());
                } else {
                    Logger.getLogger(ObserverManager.class.getName()).log(Level.SEVERE, "error invoking " + invocation.observer, t);
                }
            }
        }
    }

    /**
     * @version $Rev$ $Date$
     */
    public static class Observer {
        private final Map<Class, MetaMethod> methods = new HashMap<Class, MetaMethod>();
        private final Object observer;
        private final Class<?> observerClass;
        private final MetaMethod defaultMethod;

        public Observer(Object observer) {
            if (observer == null) throw new IllegalArgumentException("observer cannot be null");

            this.observer = observer;
            this.observerClass = observer.getClass();
            for (final Method method : observer.getClass().getMethods()) {
                final Observes annotation = isObserver(method);
                if (annotation == null) {
                    continue;
                }

                if (method.getParameterTypes().length > 1) {
                    throw new IllegalArgumentException("@Observes method must have only 1 parameter: " + method.toString());
                }

                if (Modifier.isAbstract(method.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method must not be abstract: " + method.toString());
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method must be public: " + method.toString());
                }

                final Class<?> type = method.getParameterTypes()[0];

                if (type.isAnnotation()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an annotation): " + method.toString());
                }

                if (Modifier.isAbstract(type.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an abstract class): " + method.toString());
                }

                if (type.isInterface()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an interface): " + method.toString());
                }

                if (type.isArray()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an array): " + method.toString());
                }

                if (type.isPrimitive()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not a primitive): " + method.toString());
                }

                methods.put(type, new MetaMethod(method, new CopyOnWriteArraySet<Class<?>>(asList(annotation.after()))));
            }

            defaultMethod = methods.get(Object.class);

            if (methods.size() == 0) {
                throw new NotAnObserverException("Object has no @Observes methods. For example: public void observe(@Observes RetryConditionAdded event){...}");
            }
        }

        public Invocation toInvocation(final Object event) {
            if (event == null) throw new IllegalArgumentException("event cannot be null");

            final Class eventType = event.getClass();
            final MetaMethod method = methods.get(eventType);

            if (method != null) {
                return new Invocation(this, method, event);
            } else if (defaultMethod != null) {
                return new Invocation(this, defaultMethod, event);
            }
            return null;
        }

        public Class<?> getObserverClass() {
            return observerClass;
        }

        private Observes isObserver(Method method) {
            for (final Annotation[] annotations : method.getParameterAnnotations()) {
                for (final Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Observes.class)) {
                        return Observes.class.cast(annotation);
                    }
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Observer observer1 = (Observer) o;

            return observer.equals(observer1.observer);
        }

        @Override
        public int hashCode() {
            return observer.hashCode();
        }

        @Override
        public String toString() {
            return "Observer{" +
                    "class=" + observer.getClass().getName() +
                    '}';
        }
    }

    private static class MetaMethod {
        private final Method method;
        private final Collection<Class<?>> after;

        private MetaMethod(final Method method, final Collection<Class<?>> after) {
            this.method = method;
            this.after = after;
        }

        @Override
        public String toString() {
            return "MetaMethod{" +
                    "method=" + method +
                    ", after=" + after +
                    '}';
        }
    }

    private static class Invocation implements Comparable<Invocation> {
        private final Observer observer;
        private final MetaMethod metaMethod;
        private final Object event;

        private Invocation(final Observer observer, final MetaMethod method, final Object event) {
            this.observer = observer;
            this.metaMethod = method;
            this.event = event;
        }

        private void proceed() throws InvocationTargetException, IllegalAccessException {
            metaMethod.method.invoke(observer.observer, event);
        }

        @Override // only called for a single event so only consider observer.after
        public int compareTo(final Invocation o) {
            if (o == null) {
                return 0;
            }
            if (metaMethod.after.contains(o.observer.observerClass)) {
                return 1;
            }
            return -1;
        }

        @Override
        public String toString() {
            return "Invocation{" +
                    "observer=" + observer +
                    ", metaMethod=" + metaMethod +
                    ", event=" + event +
                    '}';
        }
    }

    private static class NotAnObserverException extends RuntimeException {
        public NotAnObserverException(final String s) {
            super(s);
        }
    }
}
