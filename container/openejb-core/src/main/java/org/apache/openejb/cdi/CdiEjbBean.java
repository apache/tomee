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

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.webbeans.component.BeanAttributesImpl;
import org.apache.webbeans.component.InterceptedMarker;
import org.apache.webbeans.component.creation.BeanAttributesBuilder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionTargetFactoryImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.portable.InjectionTargetImpl;

import javax.ejb.NoSuchEJBException;
import javax.ejb.Remove;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.SessionBeanType;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CdiEjbBean<T> extends BaseEjbBean<T> implements InterceptedMarker {
    private final Map<Integer, Object> dependentSFSBToBeRemoved = new ConcurrentHashMap<Integer, Object>();

    private final BeanContext beanContext;

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext, AnnotatedType<T> at) {
        this(beanContext, webBeansContext, beanContext.getManagedClass(), at, new EjbInjectionTargetFactory<T>(at, webBeansContext));
        EjbInjectionTargetImpl.class.cast(getInjectionTarget()).setCdiEjbBean(this);
    }

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext, Class beanClass, AnnotatedType<T> at, InjectionTargetFactoryImpl<T> factory) {
        super(webBeansContext, toSessionType(beanContext.getComponentType()), at, new EJBBeanAttributesImpl<T>(beanContext,
                BeanAttributesBuilder.forContext(webBeansContext).newBeanAttibutes(at).build()), beanClass, factory);
        this.beanContext = beanContext;
        beanContext.set(Bean.class, this);
        passivatingId = beanContext.getDeploymentID() + getReturnType().getName();
    }

    public BeanContext getBeanContext() {
        return this.beanContext;
    }

    private static SessionBeanType toSessionType(BeanType beanType) {
        switch (beanType) {
        case SINGLETON:
            return SessionBeanType.SINGLETON;
        case MESSAGE_DRIVEN: // OWB implementation test stateful or not so do we really care?
        case STATELESS:
        case MANAGED: // can't be stateful since it will prevent every integration using ManagedBean to get injections to work + it is never used
            return SessionBeanType.STATELESS;
        case STATEFUL:
            return SessionBeanType.STATEFUL;
        default:
            throw new IllegalStateException("Unknown Session BeanType " + beanType);
        }
    }

    public String getEjbName() {
        return this.beanContext.getEjbName();
    }

    public boolean needsBeanLocalViewAddedToTypes() {
        return beanContext.isLocalbean() && beanContext.getBeanClass().getAnnotation(Typed.class) == null;
    }

    @Override
    public List<Class<?>> getBusinessLocalInterfaces() {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        for (final Type t : getTypes()) {
            if (Class.class.isInstance(t)) {
                classes.add(Class.class.cast(t));
            }
        }
        return classes;
    }

    public void destroyComponentInstance(final T instance) {
        if (getScope() == null || Dependent.class == getScope()) {
            destroyStatefulSessionBeanInstance(instance, null);
        } else {
            destroyScopedStateful(instance, null);
        }
    }

    @Override
    protected void destroyStatefulSessionBeanInstance(final T proxyInstance, final Object unused) {
        if (proxyInstance instanceof BeanContext.Removable) {
            try {
                ((BeanContext.Removable) proxyInstance).$$remove();
            } catch (NoSuchEJBException nsee) {
                // no-op
            } catch (UndeclaredThrowableException nsoe) {
                if (!(nsoe.getCause() instanceof NoSuchObjectException)) {
                    throw nsoe;
                }
            } catch (Exception e) {
                if (!(e instanceof NoSuchObjectException)) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new OpenEJBRuntimeException(e);
                }
            }
        }
    }

    @Override
    public List<Method> getRemoveMethods() {
        // Should we delegate to super and merge both?
        if (beanContext.isLocalbean()) {
            return findRemove(beanContext.getBeanClass(), beanContext.getBeanClass());
        }
        return findRemove(beanContext.getBeanClass(), beanContext.getBusinessLocalInterface());
    }

    private List<Method> findRemove(final Class<?> beanClass, final Class<?> beanInterface) {
        List<Method> toReturn = new ArrayList<Method>();

        // Get all the public methods of the bean class and super class
        Method[] methods = beanClass.getMethods();

        // Search for methods annotated with @Remove
        for (Method method : methods) {
            Remove annotation = method.getAnnotation(Remove.class);
            if (annotation != null) {
                // Get the corresponding method into the bean interface
                Method interfaceMethod;
                try {
                    interfaceMethod = beanInterface.getMethod(method.getName(), method.getParameterTypes());
                    toReturn.add(interfaceMethod);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // The method can not be into the interface in which case we
                    // don't wonder of
                }
            }
        }

        return toReturn;
    }

    protected T createEjb(final CreationalContext<T> creationalContext) {
        final List<Class> classes = beanContext.getBusinessLocalInterfaces();
        final CurrentCreationalContext currentCreationalContext = beanContext.get(CurrentCreationalContext.class);
        final CreationalContext existing = currentCreationalContext.get();
        currentCreationalContext.set(creationalContext);
        try {
            final T instance;
            if (classes.size() == 0 && beanContext.isLocalbean()) {
                final BeanContext.BusinessLocalBeanHome home = beanContext.getBusinessLocalBeanHome();
                instance = (T) home.create();
            } else {
                final Class<?> mainInterface = classes.get(0);
                final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, classes);
                final BeanContext.BusinessLocalHome home = beanContext.getBusinessLocalHome(interfaces, mainInterface);
                instance = (T) home.create();
            }

            if (getScope().equals(Dependent.class) && BeanType.STATEFUL.equals(beanContext.getComponentType())) {
                CreationalContextImpl.class.cast(creationalContext).addDependent(this, instance);
            }

            return instance;
        } finally {
            currentCreationalContext.set(existing);
        }
    }

    private void destroyScopedStateful(final T instance, final CreationalContext<T> cc) {
        try {
            instance.hashCode(); // force the instance to be created - otherwise we'll miss @PreDestroy for instance
        } catch (final NoSuchEJBException e) {
            InvocationHandler handler = null;
            if (LocalBeanProxyFactory.isProxy(instance.getClass())) {
                handler = LocalBeanProxyFactory.getInvocationHandler(instance);
            } else if (ProxyManager.isProxyClass(instance.getClass())) {
                handler = ProxyManager.getInvocationHandler(instance);
            }
            if (BaseEjbProxyHandler.class.isInstance(handler) && !BaseEjbProxyHandler.class.cast(handler).isValid()) {
                return; // already destroyed
            } // else log error

            logger.log(Level.FINE, "The stateful instance " + instance + " can't be removed since it was invalidated", e);
            return;
        }

        Object ejbInstance = dependentSFSBToBeRemoved.remove(System.identityHashCode(instance));
        if (ejbInstance != null) {
            destroyStatefulSessionBeanInstance((T) ejbInstance, cc);
        } else {
            destroyStatefulSessionBeanInstance(instance, cc);
        }
    }

    public void storeStatefulInstance(final Object proxy, final T instance) {
        dependentSFSBToBeRemoved.put(System.identityHashCode(proxy), instance);
    }

    private static class EJBBeanAttributesImpl<T> extends BeanAttributesImpl<T> {
        private final BeanContext beanContext;
        private final Set<Type> ejbTypes;

        public EJBBeanAttributesImpl(final BeanContext bc, final BeanAttributesImpl<T> beanAttributes) {
            super(beanAttributes, false);
            this.beanContext = bc;
            this.ejbTypes = new HashSet<Type>();
            initTypes();
        }

        @Override
        public Set<Type> getTypes() {
            return ejbTypes;
        }

        public void initTypes() {
            if (beanContext.isLocalbean()) {
                addApiTypes(ejbTypes, beanContext.getBeanClass());
            }

            if (beanContext.getProxyClass() != null) {
                addApiTypes(ejbTypes, beanContext.getProxyClass());
            }

            final List<Class> cl = beanContext.getBusinessLocalInterfaces();
            if (cl != null && !cl.isEmpty()) {
                for (final Class<?> c : cl) {
                    ejbTypes.add(c);
                }
            }

            if (!ejbTypes.contains(Serializable.class)) {
                ejbTypes.add(Serializable.class);
            }

            ejbTypes.add(Object.class);
        }

        private static void addApiTypes(final Collection<Type> clazzes, final Class<?> beanClass) {
            final Typed typed = beanClass.getAnnotation(Typed.class);
            if (typed == null || typed.value().length == 0) {
                Class<?> current = beanClass;
                while (current != null && !Object.class.equals(current)) {
                    clazzes.add(current);
                    current = current.getSuperclass();
                }
            } else {
                Collections.addAll(clazzes, typed.value());
            }
        }
    }

    public static class EjbInjectionTargetFactory<T> extends InjectionTargetFactoryImpl<T> {
        public EjbInjectionTargetFactory(final AnnotatedType<T> annotatedType, final WebBeansContext webBeansContext) {
            super(annotatedType, webBeansContext);
        }

        @Override
        public InjectionTarget<T> createInjectionTarget(Bean<T> bean) {
            final EjbInjectionTargetImpl<T> injectionTarget = new EjbInjectionTargetImpl<T>(getAnnotatedType(), createInjectionPoints(bean), getWebBeansContext());
            final InjectionTarget<T> it = getWebBeansContext().getWebBeansUtil().fireProcessInjectionTargetEvent(injectionTarget, getAnnotatedType()).getCompleteInjectionTarget();
            if (!EjbInjectionTargetImpl.class.isInstance(it)) {
                return new EjbInjectionTargetImpl<T>(injectionTarget, it);
            }
            return it;
        }

        protected Set<InjectionPoint> createInjectionPoints(Bean<T> bean) {
            final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
            for (InjectionPoint injectionPoint: getWebBeansContext().getInjectionPointFactory().buildInjectionPoints(bean, getAnnotatedType())) {
                injectionPoints.add(injectionPoint);
            }
            return injectionPoints;
        }

        protected List<AnnotatedMethod<?>> getPostConstructMethods() {
            return Collections.emptyList();
        }

        protected List<AnnotatedMethod<?>> getPreDestroyMethods() {
            return Collections.emptyList();
        }
    }

    public static class EjbInjectionTargetImpl<T> extends InjectionTargetImpl<T> {
        private CdiEjbBean<T> bean;
        private InjectionTarget<T> delegate = null;

        public EjbInjectionTargetImpl(AnnotatedType<T> annotatedType, Set<InjectionPoint> points, WebBeansContext webBeansContext) {
            super(annotatedType, points, webBeansContext,
                    Collections.<AnnotatedMethod<?>>emptyList(), Collections.<AnnotatedMethod<?>>emptyList());
        }

        public EjbInjectionTargetImpl(final EjbInjectionTargetImpl<T> original, final InjectionTarget<T> delegate) {
            super(original.annotatedType, original.getInjectionPoints(), original.webBeansContext, Collections.<AnnotatedMethod<?>>emptyList(), Collections.<AnnotatedMethod<?>>emptyList());
            this.delegate = delegate;
        }

        public void setCdiEjbBean(final CdiEjbBean<T> bean) {
            this.bean = bean;
        }

        @Override
        public T produce(final CreationalContext<T> creationalContext) {
            if (delegate == null) {
                return bean.createEjb(creationalContext);
            }
            return delegate.produce(creationalContext);
        }

        @Override
        public void dispose(final T instance) {
            if (delegate == null) {
                bean.destroyComponentInstance(instance);
            } else {
                delegate.dispose(instance);
            }
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            if (delegate == null) {
                return super.getInjectionPoints();
            }
            return delegate.getInjectionPoints();
        }

        @Override
        public void inject(final T instance, final CreationalContext<T> ctx) {
            if (delegate == null) {
                super.inject(instance, ctx);
            } else {
                delegate.inject(instance, ctx);
            }
        }

        @Override
        public void postConstruct(final T instance) {
            if (delegate == null) {
                super.postConstruct(instance);
            } else {
                delegate.postConstruct(instance);
            }
        }

        @Override
        public void preDestroy(final T instance) {
            if (delegate == null) {
                super.preDestroy(instance);
            } else {
                delegate.preDestroy(instance);
            }
        }

        public T createNewPojo(final CreationalContext<T> creationalContext) {
            return super.newInstance(CreationalContextImpl.class.cast(creationalContext));
        }
    }
}
