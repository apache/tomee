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
package org.apache.openejb.cdi;

import org.apache.webbeans.component.EventBean;
import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.component.InstanceBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansAnnotatedTypeUtil;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * It seems it is the same class than the OWB one??
 *
 * @version $Rev$ $Date$
 */
public final class OWBInjector {

    /**
     * Creational context to hold dependent instances
     */
    private CreationalContextImpl<?> ownerCreationalContext = null;

    /**
     * Underlying javaee instance
     */
    private Object javaEEInstance;

    private final WebBeansContext webBeansContext;

    public OWBInjector() {
        this(WebBeansContext.currentInstance());
    }

    /**
     * Creates a new instance
     *
     * @param webBeansContext the OWB context
     */
    public OWBInjector(WebBeansContext webBeansContext) {
        //No operation
        this.webBeansContext = webBeansContext;
    }

    /**
     * Inject dependencies of given instance.
     *
     * @param javaEeComponentInstance instance
     * @return this injector
     * @throws Exception if exception occurs
     */
    public OWBInjector inject(Object javaEeComponentInstance) throws Exception {
        return inject(javaEeComponentInstance, null);
    }

    /**
     * Inject dependencies of given instance.
     *
     * @param javaEeComponentInstance instance
     * @param creationalContext       context
     * @return this injector
     * @throws Exception if exception occurs
     */
    @SuppressWarnings("unchecked")
    public OWBInjector inject(Object javaEeComponentInstance, CreationalContext<?> creationalContext) throws Exception {
        BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();
        this.javaEEInstance = javaEeComponentInstance;
        if (creationalContext == null) {
            this.ownerCreationalContext = (CreationalContextImpl<?>) beanManager.createCreationalContext(null);
        }

        Class<Object> injectableComponentClass = (Class<Object>) javaEeComponentInstance.getClass();

        //Look for custom InjectionTarget
        InjectionTargetWrapper<Object> wrapper = beanManager.getInjectionTargetWrapper(injectableComponentClass);
        if (wrapper != null) {
            wrapper.inject(javaEeComponentInstance, (CreationalContext<Object>) this.ownerCreationalContext);
            return this;
        }

        AnnotatedType<Object> annotated = beanManager.createAnnotatedType(injectableComponentClass);
        Set<InjectionPoint> injectionPoints = WebBeansAnnotatedTypeUtil.getJavaEeComponentInstanceInjectionPoints(webBeansContext, annotated);
        if (injectionPoints != null && injectionPoints.size() > 0) {
            for (InjectionPoint injectionPoint : injectionPoints) {
                if (injectionPoint.getMember() instanceof Method) {
                    Method method = (Method) injectionPoint.getMember();

                    //Get injected method arguments
                    List<Object> parameters = getInjectedMethodParameterReferences(injectionPoint, beanManager, injectionPoints);

                    //Set method
                    ClassUtil.callInstanceMethod(method, javaEeComponentInstance, parameters.toArray(new Object[parameters.size()]));

                } else if (injectionPoint.getMember() instanceof Field) {
                    //Get injected object ref
                    Object object = getInjectedObjectReference(injectionPoint, beanManager);

                    //Set field
                    Field field = (Field) injectionPoint.getMember();

                    try {
                        field.setAccessible(true);
                        field.set(javaEeComponentInstance, object);
                    } catch (Exception e) {
                        throw new WebBeansException(e);
                    }
                }
            }

            return this;
        }

        return null;
    }

    /**
     * Release dependents.
     */
    @SuppressWarnings("unchecked")
    public void destroy() {
        BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();

        //Look for custom InjectionTarget
        InjectionTargetWrapper<Object> wrapper = beanManager.getInjectionTargetWrapper((Class<Object>) javaEEInstance.getClass());
        if (wrapper != null) {
            wrapper.dispose(javaEEInstance);
            this.javaEEInstance = null;
            this.ownerCreationalContext = null;
        } else {
            if (this.ownerCreationalContext != null) {
                this.ownerCreationalContext.release();
                this.ownerCreationalContext = null;
            }
        }
    }

    /**
     * Gets injected object reference.
     *
     * @param injectionPoint injection point of javaee instance
     * @param beanManager    bean manager implementation
     * @return injected reference
     */
    private Object getInjectedObjectReference(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        Object object;

        //Injected contextual beam
        InjectionResolver injectionResolver = beanManager.getInjectionResolver();

        Bean<?> injectedBean = injectionResolver.getInjectionPointBean(injectionPoint);

        if (isInstanceProviderInjection(injectionPoint)) {
            InstanceBean.local.set(injectionPoint);
        } else if (isEventProviderInjection(injectionPoint)) {
            EventBean.local.set(injectionPoint);
        } else if (WebBeansUtil.isDependent(injectedBean)) {
            if (!InjectionPoint.class.isAssignableFrom(ClassUtil.getClass(injectionPoint.getType()))) {
                InjectionPointBean.setThreadLocal(injectionPoint);
            }
        }

        object = beanManager.getInjectableReference(injectionPoint, ownerCreationalContext);

        return object;
    }

    /**
     * Gets initializer method parameters.
     *
     * @param injectionPoint  javaee component
     *                        injection point
     * @param beanManager     bean manager
     * @param injectionPoints all injection points
     * @return injected method injected arguments
     */
    private List<Object> getInjectedMethodParameterReferences(InjectionPoint injectionPoint, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints) {
        Method method = (Method) injectionPoint.getMember();
        List<InjectionPoint> injectedPoints = getInjectedPoints(method, injectionPoints);
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < injectedPoints.size(); i++) {
            for (InjectionPoint point : injectedPoints) {
                AnnotatedParameter<?> parameter = (AnnotatedParameter<?>) point.getAnnotated();
                if (parameter.getPosition() == i) {
                    Object instance = getInjectedObjectReference(injectionPoint, beanManager);
                    list.add(instance);
                    break;
                }
            }
        }

        return list;
    }

    /**
     * Gets injection point of given methods.
     *
     * @param method          injection point member
     * @param injectionPoints all injection points
     * @return method injection points
     */
    private List<InjectionPoint> getInjectedPoints(Method method, Set<InjectionPoint> injectionPoints) {
        List<InjectionPoint> points = new ArrayList<InjectionPoint>();

        for (InjectionPoint ip : injectionPoints) {
            if (ip.getMember().equals(method)) {
                points.add(ip);
            }
        }

        return points;

    }

    /**
     * Returns true if injection point is instance injection point
     * false otherwise.
     *
     * @param injectionPoint injection point
     * @return true if injection point is instance injection point
     */
    private boolean isInstanceProviderInjection(InjectionPoint injectionPoint) {
        Type type = injectionPoint.getType();

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> clazz = (Class<?>) pt.getRawType();

            if (Provider.class.isAssignableFrom(clazz)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if injection point is event injection point
     * false otherwise.
     *
     * @param injectionPoint injection point
     * @return true if injection point is event injection point
     */
    private boolean isEventProviderInjection(InjectionPoint injectionPoint) {
        Type type = injectionPoint.getType();

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> clazz = (Class<?>) pt.getRawType();

            if (clazz.isAssignableFrom(Event.class)) {
                return true;
            }
        }

        return false;
    }


}
