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
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObserverManager {

    private static final ThreadLocal<Set<Invocation>> SEEN = new ThreadLocal<Set<Invocation>>() {
        @Override
        protected Set<Invocation> initialValue() {
            return new HashSet<>();
        }
    };

    // lazy init since it is used in SystemInstance
    private static final AtomicReference<Logger> LOGGER = new AtomicReference<>();
    private final Set<Observer> observers = new LinkedHashSet<>();
    private final Map<Class, Invocation> methods = new ConcurrentHashMap<>();

    public boolean addObserver(final Object observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }

        try {
            final Observer wrapper = new Observer(observer);
            if (wrapper.after.size() + wrapper.before.size() + wrapper.methods.size() > 0 && observers.add(wrapper)) {
                methods.clear();
                fireEvent(new ObserverAdded(observer));
                return true;
            } else {
                return false;
            }
        } catch (final NotAnObserverException naoe) {
            return false;
        }
    }

    public boolean removeObserver(final Object observer) {
        if (observer == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        try {
            if (observers.remove(new Observer(observer))) {
                methods.clear();
                fireEvent(new ObserverRemoved(observer));
                return true;
            } else {
                return false;
            }
        } catch (final NotAnObserverException naoe) {
            return false;
        }
    }

    public <E> E fireEvent(final E event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }

        try {
            return doFire(event);
        } finally {
            SEEN.remove();
        }
    }

    private <E> E doFire(final E event) {
        final Class<?> type = event.getClass();

        final Invocation invocation = getInvocation(type);

        invocation.invoke(event);

        return event;
    }

    private Invocation getInvocation(final Class<?> type) {
        {
            final Invocation invocation = methods.get(type);
            if (invocation != null) {
                return invocation;
            }
        }

        final Invocation invocation = buildInvocation(type);
        methods.put(type, invocation);
        return invocation;
    }

    public void destroy() {
        for (final Observer o : new LinkedList<>(observers)) {
            removeObserver(o.observer);
        }
    }

    private enum Phase {
        BEFORE,
        INVOKE,
        AFTER
    }

    private Invocation buildInvocation(final Class<?> type) {
        final Invocation before = buildInvocation(Phase.BEFORE, type);
        final Invocation after = buildInvocation(Phase.AFTER, type);
        final Invocation invoke = buildInvocation(Phase.INVOKE, type);

        if (IGNORE == before && IGNORE == after) {

            return invoke;

        } else {

            return new BeforeAndAfterInvocationSet(before, invoke, after);
        }
    }

    private Invocation buildInvocation(final Phase phase, final Class<?> type) {

        final InvocationList list = new InvocationList();

        for (final Observer observer : observers) {

            final Invocation method = observer.get(phase, type);

            if (method != null && method != IGNORE) {

                list.add(method);

            }
        }

        switch (list.getInvocations().size()) {
            case 0:
                return IGNORE;
            case 1:
                return list.getInvocations().get(0);
            default:
                return list;
        }
    }

    /**
     * @version $Rev$ $Date$
     */
    public class Observer {

        private final Map<Class, Invocation> before = new ConcurrentHashMap<>();
        private final Map<Class, Invocation> methods = new ConcurrentHashMap<>();
        private final Map<Class, Invocation> after = new ConcurrentHashMap<>();
        private final Object observer;

        public Observer(final Object observer) {
            if (observer == null) {
                throw new IllegalArgumentException("observer cannot be null");
            }

            final Set<Method> methods = new HashSet<>();
            methods.addAll(Arrays.asList(observer.getClass().getMethods()));
            methods.addAll(Arrays.asList(observer.getClass().getDeclaredMethods()));

            this.observer = observer;
            for (final Method method : methods) {
                if (!isObserver(method)) {
                    continue;
                }

                if (method.getParameterTypes().length > 1) {
                    throw new IllegalArgumentException("@Observes method must have only 1 parameter: " + method.toString());
                }

                if (Modifier.isAbstract(method.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method must not be abstract: " + method.toString());
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method must not be static: " + method.toString());
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException("@Observes method must be public: " + method.toString());
                }

                final Class<?> type = method.getParameterTypes()[0];

                if (AfterEvent.class.equals(type)) {

                    final Class parameterClass = getParameterClass(method);
                    this.after.put(parameterClass, new AfterInvocation(method, observer));

                } else if (BeforeEvent.class.equals(type)) {

                    final Class parameterClass = getParameterClass(method);
                    this.before.put(parameterClass, new BeforeInvocation(method, observer));

                } else {

                    validate(method, type);
                    this.methods.put(type, new MethodInvocation(method, observer));

                }
            }

            if (methods.isEmpty() && after.isEmpty() && before.isEmpty()) {
                throw new NotAnObserverException("Object has no @Observes methods. For example: public void observe(@Observes RetryConditionAdded event){...}");
            }
        }

        private Class getParameterClass(final Method method) {

            final Type[] genericParameterTypes = method.getGenericParameterTypes();

            final Type generic = genericParameterTypes[0];

            if (!(generic instanceof ParameterizedType)) {
                final Class<?> event = method.getParameterTypes()[0];
                throw new IllegalArgumentException("@Observes " + event.getSimpleName() + " missing generic type: " + method.toString());
            }

            final ParameterizedType parameterized = ParameterizedType.class.cast(generic);

            final Type type = parameterized.getActualTypeArguments()[0];

            final Class clazz;

            if (type instanceof Class) {

                clazz = Class.class.cast(type);

            } else if (type instanceof WildcardType) {

                clazz = Object.class;

            } else {

                final Class<?> event = method.getParameterTypes()[0];
                throw new IllegalArgumentException("@Observes " + event.getSimpleName() + " unsupported generic type: " + type.getClass().getSimpleName() + "  " + method.toString());
            }

            validate(method, clazz);

            return clazz;
        }

        private void validate(final Method method, final Class<?> type) {
            if (type.isAnnotation()) {
                throw new IllegalArgumentException("@Observes method parameter must be a concrete class (not an annotation): " + method.toString());
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
        }

        private Map<Class, Invocation> map(final Phase event) {
            switch (event) {
                case AFTER:
                    return after;
                case BEFORE:
                    return before;
                case INVOKE:
                    return methods;
                default:
                    throw new IllegalStateException("Unknown Event style " + event);
            }
        }

        public Invocation get(final Phase event, final Class eventType) {
            return get(map(event), eventType);
        }

        public Invocation getAfter(final Class eventType) {
            return get(after, eventType);
        }

        public Invocation getBefore(final Class eventType) {
            return get(before, eventType);
        }

        private Invocation get(final Map<Class, Invocation> map, final Class eventType) {
            if (eventType == null) {
                return IGNORE;
            }

            final Invocation method = map.get(eventType);

            if (method != null) {
                return method;
            }

            return get(map, eventType.getSuperclass());
        }

        private boolean isObserver(final Method method) {
            for (final Annotation[] annotations : method.getParameterAnnotations()) {
                for (final Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Observes.class)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Observer observer1 = (Observer) o;

            return observer.equals(observer1.observer);
        }

        @Override
        public int hashCode() {
            return observer.hashCode();
        }
    }

    public interface Invocation {

        void invoke(Object event);

    }


    private static final Invocation IGNORE = new Invocation() {
        @Override
        public void invoke(final Object event) {
        }

        @Override
        public String toString() {
            return "IGNORE";
        }
    };


    public class MethodInvocation implements Invocation {
        private final Method method;
        private final Object observer;

        public MethodInvocation(final Method method, final Object observer) {
            this.method = method;
            this.observer = observer;
        }

        @Override
        public void invoke(final Object event) {
            try {
                method.invoke(observer, event);
            } catch (final InvocationTargetException e) {
                if (!SEEN.get().add(this)) {
                    return;
                }

                final Throwable t = e.getTargetException() == null ? e : e.getTargetException();

                if (!(event instanceof ObserverFailed)) {
                    doFire(new ObserverFailed(observer, method, event, t));
                }

                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    ObserverManager.logger().log(Level.SEVERE, "error invoking " + observer, t.getCause());
                } else {
                    ObserverManager.logger().log(Level.SEVERE, "error invoking " + observer, t);
                }
            } catch (final IllegalAccessException e) {
                ObserverManager.logger().log(Level.SEVERE, method + " can't be invoked, check it is public");
            }
        }

        @Override
        public String toString() {
            return method.toString();
        }
    }

    // done lazily since this class is used in SystemInstance
    private static Logger logger() {
        Logger value = LOGGER.get();
        if (value == null) {
            value = Logger.getLogger(ObserverManager.class.getName());
            LOGGER.set(value);
        }
        return value;
    }

    private final class AfterInvocation extends MethodInvocation {

        private AfterInvocation(final Method method, final Object observer) {
            super(method, observer);
        }

        @Override
        public void invoke(final Object event) {
            super.invoke(new AfterEvent() {
                @Override
                public Object getEvent() {
                    return event;
                }

                @Override
                public String toString() {
                    return "AfterEvent{} " + event;
                }
            });
        }
    }

    private final class BeforeInvocation extends MethodInvocation {

        private BeforeInvocation(final Method method, final Object observer) {
            super(method, observer);
        }

        @Override
        public void invoke(final Object event) {
            super.invoke(new BeforeEvent() {
                @Override
                public Object getEvent() {
                    return event;
                }

                @Override
                public String toString() {
                    return "BeforeEvent{} " + event;
                }
            });
        }
    }

    private static final class BeforeAndAfterInvocationSet implements Invocation {

        private final Invocation before;
        private final Invocation invoke;
        private final Invocation after;

        private BeforeAndAfterInvocationSet(final Invocation before, final Invocation invoke, final Invocation after) {
            this.before = before;
            this.invoke = invoke;
            this.after = after;
        }

        @Override
        public void invoke(final Object event) {
            before.invoke(event);
            invoke.invoke(event);
            after.invoke(event);
        }
    }

    public static class InvocationList implements Invocation {

        private final List<Invocation> invocations = new LinkedList<>();

        public boolean add(final Invocation invocation) {
            return invocations.add(invocation);
        }

        public List<Invocation> getInvocations() {
            return invocations;
        }

        @Override
        public void invoke(final Object event) {
            for (final Invocation invocation : invocations) {
                invocation.invoke(event);
            }
        }
    }

    public static class NotAnObserverException extends IllegalArgumentException {
        public NotAnObserverException(final String s) {
            super(s);
        }
    }
}
