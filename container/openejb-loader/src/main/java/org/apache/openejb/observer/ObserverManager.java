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

import org.apache.openejb.observer.event.AfterEvent;
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.openejb.observer.event.ObserverAdded;
import org.apache.openejb.observer.event.ObserverFailed;
import org.apache.openejb.observer.event.ObserverRemoved;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            doFire(new ObserverAdded(observer));
        }
        return added;
    }

    public boolean removeObserver(Object observer) {
        if (observer == null) throw new IllegalArgumentException("observer cannot be null");

        final boolean removed = observers.remove(new Observer(observer));
        if (removed) {
            // Observers can observe they are to be removed
            doFire(new ObserverRemoved(observer));
        }
        return removed;
    }

    public <T> T fireEvent(final T event) {
        if (event == null) throw new IllegalArgumentException("event cannot be null");

        doFire(new BeforeEventImpl<T>(event));
        doFire(event);
        doFire(new AfterEventImpl<T>(event));
        return event;
    }

    private void doFire(final Object event) {
        final List<Invocation> invocations = new LinkedList<Invocation>();
        for (final Observer observer : observers) {
            final Invocation i = observer.toInvocation(event);
            if (i != null) {
                invocations.add(i);
            }
        }

        for (final Invocation invocation : invocations) {
            try {
                invocation.proceed();
            } catch (final Throwable t) {
                if (!(event instanceof ObserverFailed) && !AfterEventImpl.class.isInstance(event)) {
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
        private final Map<Type, Method> methods = new HashMap<Type, Method>();
        private final Object observer;
        private final Class<?> observerClass;
        private final Method defaultMethod;

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

                final Type type = method.getGenericParameterTypes()[0];
                final Class<?> raw = method.getParameterTypes()[0];

                if (raw.isAnnotation()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an annotation): " + method.toString());
                }

                if (Modifier.isAbstract(raw.getModifiers()) && raw == type) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an abstract class): " + method.toString());
                }

                if (raw.isInterface() && raw == type) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an interface): " + method.toString());
                }

                if (raw.isArray()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an array): " + method.toString());
                }

                if (raw.isPrimitive()) {
                    throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not a primitive): " + method.toString());
                }

                methods.put(type, method);
            }

            defaultMethod = methods.get(Object.class);

            if (methods.size() == 0) {
                throw new NotAnObserverException("Object has no @Observes methods. For example: public void observe(@Observes RetryConditionAdded event){...}");
            }
        }

        public Invocation toInvocation(final Object event) {
            if (event == null) throw new IllegalArgumentException("event cannot be null");

            final Class eventType = event.getClass();
            final Method method = methods.get(eventType);

            if (method == null && AfterEventImpl.class.isInstance(event)) {
                final Type type = new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] { AfterEventImpl.class.cast(event).getEvent().getClass() };
                    }

                    @Override
                    public Type getRawType() {
                        return AfterEvent.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };
                for (final Map.Entry<Type, Method> m : methods.entrySet()) {
                    if (m.getKey().equals(type)) {
                        return new Invocation(this, m.getValue(), event);
                    }
                }
            } else if (method == null && BeforeEventImpl.class.isInstance(event)) {
                final Type type = new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] { BeforeEventImpl.class.cast(event).getEvent().getClass() };
                    }

                    @Override
                    public Type getRawType() {
                        return BeforeEvent.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };
                for (final Map.Entry<Type, Method> m : methods.entrySet()) {
                    if (m.getKey().equals(type)) {
                        return new Invocation(this, m.getValue(), event);
                    }
                }
            }

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

    private static class Invocation {
        private final Observer observer;
        private final Method method;
        private final Object event;

        private Invocation(final Observer observer, final Method method, final Object event) {
            this.observer = observer;
            this.method = method;
            this.event = event;
        }

        private void proceed() throws InvocationTargetException, IllegalAccessException {
            method.invoke(observer.observer, event);
        }

        @Override
        public String toString() {
            return "Invocation{" +
                    "observer=" + observer +
                    ", method=" + method +
                    ", event=" + event +
                    '}';
        }
    }

    private static class NotAnObserverException extends RuntimeException {
        public NotAnObserverException(final String s) {
            super(s);
        }
    }

    private static class AfterEventImpl<T> implements AfterEvent<T> {
        private final T event;

        public AfterEventImpl(final T event) {
            this.event = event;
        }

        public T getEvent() {
            return event;
        }
    }

    private static class BeforeEventImpl<T> implements BeforeEvent<T> {
        private final T event;

        public BeforeEventImpl(final T event) {
            this.event = event;
        }

        public T getEvent() {
            return event;
        }
    }
}
