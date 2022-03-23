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

package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;
import org.apache.openejb.util.SetAccessible;
import org.apache.webbeans.component.CdiInterceptorBean;
import org.apache.xbean.finder.ClassFinder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.AfterBegin;
import jakarta.ejb.AfterCompletion;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorData {
    private static final Map<Class<?>, InterceptorData> CACHE = new ConcurrentHashMap<Class<?>, InterceptorData>();

    private final Class clazz;
    private final CdiInterceptorBean cdiInterceptorBean;

    private final Set<Method> aroundInvoke = new LinkedHashSet<>();

    private final Set<Method> postConstruct = new LinkedHashSet<>();
    private final Set<Method> preDestroy = new LinkedHashSet<>();

    private final Set<Method> postActivate = new LinkedHashSet<>();
    private final Set<Method> prePassivate = new LinkedHashSet<>();

    private final Set<Method> afterBegin = new LinkedHashSet<>();
    private final Set<Method> beforeCompletion = new LinkedHashSet<>();
    private final Set<Method> afterCompletion = new LinkedHashSet<>();

    private final Set<Method> aroundTimeout = new LinkedHashSet<>();

    private final Map<Class<?>, Object> data = new HashMap<>();

    public InterceptorData(final Class clazz) {
        this.clazz = clazz;
        this.cdiInterceptorBean = null;
    }

    public InterceptorData(CdiInterceptorBean cdiInterceptorBean) {
        this.cdiInterceptorBean = cdiInterceptorBean;
        this.clazz = cdiInterceptorBean.getBeanClass();
        this.aroundInvoke.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.AROUND_INVOKE));
        this.postConstruct.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.POST_CONSTRUCT));
        this.preDestroy.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.PRE_DESTROY));
        this.postActivate.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.POST_ACTIVATE));
        this.prePassivate.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.PRE_PASSIVATE));
        this.aroundTimeout.addAll(getInterceptionMethodAsListOrEmpty(cdiInterceptorBean, InterceptionType.AROUND_TIMEOUT));
        /*
         AfterBegin, BeforeCompletion and AfterCompletion are ignored since not handled by CDI
         */
    }

    private List<Method> getInterceptionMethodAsListOrEmpty(final CdiInterceptorBean cdiInterceptorBean, final InterceptionType aroundInvoke) {
        final Method[] methods = cdiInterceptorBean.getInterceptorMethods(aroundInvoke);
        return methods == null ? Collections.<Method>emptyList() : asList(methods);
    }

    /**
     * @return the CdiInterceptorBean or {@code null} if not a CDI interceptor
     */
    public CdiInterceptorBean getCdiInterceptorBean() {
        return cdiInterceptorBean;
    }

    public Class getInterceptorClass() {
        return clazz;
    }

    public Set<Method> getAroundInvoke() {
        return aroundInvoke;
    }

    public Set<Method> getPostConstruct() {
        return postConstruct;
    }

    public Set<Method> getPreDestroy() {
        return preDestroy;
    }

    public Set<Method> getPostActivate() {
        return postActivate;
    }

    public Set<Method> getPrePassivate() {
        return prePassivate;
    }

    public Set<Method> getAfterBegin() {
        return afterBegin;
    }

    public Set<Method> getBeforeCompletion() {
        return beforeCompletion;
    }

    public Set<Method> getAfterCompletion() {
        return afterCompletion;
    }

    public Set<Method> getAroundTimeout() {
        return aroundTimeout;
    }

    public Set<Method> getMethods(final Operation operation) {
        switch (operation) {
            case BUSINESS:
                return getAroundInvoke();
            case BUSINESS_WS:
                return getAroundInvoke();
            case REMOVE:
                return getAroundInvoke();
            case POST_CONSTRUCT:
                return getPostConstruct();
            case PRE_DESTROY:
                return getPreDestroy();
            case ACTIVATE:
                return getPostActivate();
            case PASSIVATE:
                return getPrePassivate();
            case AFTER_BEGIN:
                return getAfterBegin();
            case AFTER_COMPLETION:
                return getAfterCompletion();
            case BEFORE_COMPLETION:
                return getBeforeCompletion();
            case TIMEOUT:
                return getAroundTimeout();
        }
        return Collections.EMPTY_SET;
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InterceptorData that = (InterceptorData) o;

        if (!Objects.equals(clazz, that.clazz)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }

    public static void cacheScan(final Class<?> clazz) {
        CACHE.put(clazz, scan(clazz));
    }

    public static InterceptorData scan(final Class<?> clazz) {
        final InterceptorData model = CACHE.get(clazz);
        if (model != null) {
            final InterceptorData data = new InterceptorData(clazz);
            data.aroundInvoke.addAll(model.getAroundInvoke());
            data.postConstruct.addAll(model.getPostConstruct());
            data.preDestroy.addAll(model.getPreDestroy());
            data.postActivate.addAll(model.getPostActivate());
            data.prePassivate.addAll(model.getPrePassivate());
            data.afterBegin.addAll(model.getAfterBegin());
            data.beforeCompletion.addAll(model.getBeforeCompletion());
            data.afterCompletion.addAll(model.getAfterCompletion());
            data.aroundTimeout.addAll(model.getAroundTimeout());
            return data;
        }

        final ClassFinder finder = new ClassFinder(clazz);
        final InterceptorData data = new InterceptorData(clazz);

        add(finder, data.aroundInvoke, AroundInvoke.class);
        add(finder, data.postConstruct, PostConstruct.class);
        add(finder, data.preDestroy, PreDestroy.class);
        add(finder, data.postActivate, PostActivate.class);
        add(finder, data.prePassivate, PrePassivate.class);
        add(finder, data.afterBegin, AfterBegin.class);
        add(finder, data.beforeCompletion, BeforeCompletion.class);
        add(finder, data.afterCompletion, AfterCompletion.class);
        add(finder, data.aroundTimeout, AroundTimeout.class);

        return data;
    }

    private static void add(final ClassFinder finder, final Set<Method> methods, final Class<? extends Annotation> annotation) {

        final List<Method> annotatedMethods = finder.findAnnotatedMethods(annotation);
        for (final Method method : annotatedMethods) {
            SetAccessible.on(method);
            methods.add(method);
        }
    }

    public <T> void set(final Class<T> clazz, final T value) {
        data.put(clazz, value);
    }

    public <T> T get(final Class<T> clazz) {
        return clazz.cast(data.get(clazz));
    }

    @Override
    public String toString() {
        return "InterceptorData{" +
            "clazz=" + clazz.getSimpleName() + '}';
    }
}
