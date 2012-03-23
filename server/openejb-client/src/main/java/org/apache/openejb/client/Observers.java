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
package org.apache.openejb.client;

import org.apache.openejb.client.event.Observes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class Observers {
    private static final Logger logger = Logger.getLogger("OpenEJB.client");

    private final List<Observer> observers = new ArrayList<Observer>();

    public boolean addObserver(Object observer) {
        if (observer == null) throw new IllegalArgumentException("observer cannot be null");
        return observers.add(new Observer(observer));
    }

    public boolean removeObserver(Object listener) {
        if (listener == null) throw new IllegalArgumentException("listener cannot be null");
        return observers.remove(new Observer(listener));
    }

    public void fireEvent(Object event) {
        if (event == null) throw new IllegalArgumentException("event cannot be null");

        for (Observer observer : observers) {
            try {
                observer.invoke(event);
            } catch (InvocationTargetException e) {
                final Throwable t = e.getTargetException() == null ? e : e.getTargetException();

                if (e.getTargetException() != null) {
                    logger.log(Level.WARNING, "Observer method invocation failed", t);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @version $Rev$ $Date$
     */
    public static class Observer {
        private final Map<Class, Method> methods = new HashMap<Class, Method>();
        private final Object observer;
        private final Method defaultMethod;

        public Observer(Object observer) {
            if (observer == null) throw new IllegalArgumentException("observer cannot be null");

            this.observer = observer;
            for (Method method : observer.getClass().getMethods()) {
                if (!isObserver(method)) continue;

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

                methods.put(type, method);
            }

            defaultMethod = methods.get(Object.class);

            if (methods.size() == 0) {
                throw new IllegalArgumentException("Object has no @Observes methods. For example: public void observe(@Observes RetryConditionAdded event){...}");
            }
        }

        public void invoke(Object event) throws InvocationTargetException, IllegalAccessException {
            if (event == null) throw new IllegalArgumentException("event cannot be null");

            final Class eventType = event.getClass();
            final Method method = methods.get(eventType);

            if (method != null) {
                method.invoke(observer, event);
            } else if (defaultMethod != null) {
                defaultMethod.invoke(observer, event);
            }
        }

        private boolean isObserver(Method method) {
            for (Annotation[] annotations : method.getParameterAnnotations()) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Observes.class)) return true;
                }
            }
            return false;
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
    }
}
