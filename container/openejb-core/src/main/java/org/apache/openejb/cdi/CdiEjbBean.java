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
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.webbeans.component.BeanAttributesImpl;
import org.apache.webbeans.component.InterceptedMarker;
import org.apache.webbeans.config.DeploymentValidationService;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionTargetFactoryImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.InjectionTargetImpl;
import org.apache.webbeans.util.GenericsUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.decorator.Decorator;
import javax.ejb.NoSuchEJBException;
import javax.ejb.Remove;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.SessionBeanType;
import javax.interceptor.Interceptor;
import javax.transaction.UserTransaction;

public class CdiEjbBean<T> extends BaseEjbBean<T> implements InterceptedMarker, DeploymentValidationService.BeanInterceptorInfoProvider {
    private final Map<Integer, Object> dependentSFSBToBeRemoved = new ConcurrentHashMap<>();

    private final BeanContext beanContext;
    private final boolean isDependentAndStateful;
    private final boolean passivable;

    // initialized a bit later in the lifecycle but could be final otherwise
    private BeanContext.BusinessLocalBeanHome homeLocalBean;
    private BeanContext.BusinessLocalHome home;
    private BeanContext.BusinessRemoteHome remote;

    public CdiEjbBean(final BeanContext beanContext, final WebBeansContext webBeansContext, final AnnotatedType<T> at,
                      final BeanAttributes<T> attributes) {
        this(beanContext, webBeansContext, beanContext.getManagedClass(), at, new EjbInjectionTargetFactory<T>(beanContext, at, webBeansContext), attributes);
        EjbInjectionTargetImpl.class.cast(getInjectionTarget()).setCdiEjbBean(this);
    }

    public CdiEjbBean(final BeanContext bc, final WebBeansContext webBeansContext, final Class beanClass, final AnnotatedType<T> at,
                      final InjectionTargetFactoryImpl<T> factory, final BeanAttributes<T> attributes) {
        super(webBeansContext, toSessionType(bc.getComponentType()), at,
                new EJBBeanAttributesImpl<T>(bc, attributes),
                beanClass, factory);
        this.beanContext = bc;
        bc.set(Bean.class, this);
        passivatingId = bc.getDeploymentID() + getReturnType().getName();

        final boolean stateful = BeanType.STATEFUL.equals(bc.getComponentType());
        final boolean isDependent = getScope().equals(Dependent.class);
        isDependentAndStateful = isDependent && stateful;
        if (webBeansContext.getBeanManagerImpl().isPassivatingScope(getScope()) && stateful) {
            if (!getBeanContext().isPassivable()) {
                throw new DefinitionException(
                        getBeanContext().getBeanClass()
                                + " is a not apssivation-capable @Stateful with a scope "
                                + getScope().getSimpleName() + " which need passivation");
            }
            passivable = true;
        } else {
            passivable = false;
        }
        if (!isDependent) {
            for (final Type type : attributes.getTypes()) {
                if (ParameterizedType.class.isInstance(type)) {
                    throw new DefinitionException("Parameterized session bean should be @Dependent: " + beanClass);
                }
            }
        }
        if (getAnnotatedType().isAnnotationPresent(Interceptor.class) || getAnnotatedType().isAnnotationPresent(Decorator.class)) {
            throw new DefinitionException("An EJB can't be an interceptor or a decorator: " + beanClass);
        }
    }

    @Override
    public InterceptorResolutionService.BeanInterceptorInfo interceptorInfo() {
        return EjbInjectionTargetImpl.class.cast(getInjectionTarget()).getInterceptorInfo();
    }

    @Override
    public boolean isPassivationCapable() {
        return passivable;
    }

    public BeanContext getBeanContext() {
        return this.beanContext;
    }

    private static SessionBeanType toSessionType(final BeanType beanType) {
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
        final List<Class<?>> classes = new ArrayList<>();
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
            } catch (final NoSuchEJBException nsee) {
                // no-op
            } catch (final UndeclaredThrowableException nsoe) {
                if (!(nsoe.getCause() instanceof NoSuchObjectException)) {
                    throw nsoe;
                }
            } catch (final Exception e) {
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
        final List<Method> toReturn = new ArrayList<>();

        // Get all the public methods of the bean class and super class
        final Method[] methods = beanClass.getMethods();

        // Search for methods annotated with @Remove
        for (final Method method : methods) {
            final Remove annotation = method.getAnnotation(Remove.class);
            if (annotation != null) {
                // Get the corresponding method into the bean interface
                final Method interfaceMethod;
                try {
                    interfaceMethod = beanInterface.getMethod(method.getName(), method.getParameterTypes());
                    toReturn.add(interfaceMethod);
                } catch (final SecurityException e) {
                    e.printStackTrace();
                } catch (final NoSuchMethodException e) {
                    // The method can not be into the interface in which case we
                    // don't wonder of
                }
            }
        }

        return toReturn;
    }

    protected T createEjb(final CreationalContext<T> creationalContext) {
        final CurrentCreationalContext currentCreationalContext = beanContext.get(CurrentCreationalContext.class);
        final CreationalContext existing = currentCreationalContext.get();
        currentCreationalContext.set(creationalContext);
        try {
            final T instance;
            if (homeLocalBean != null) {
                instance = (T) homeLocalBean.create();
            } else if (home != null) {
                instance = (T) home.create();
            } else if (remote != null) {
                instance = (T) remote.create();
            } else { // shouldn't be called for an MDB
                throw new IllegalStateException("No interface to proxy for ejb " + beanContext.getEjbName() + ", if this is a @MessageDriven bean, try not using a scope?");
            }

            if (isDependentAndStateful) {
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

        final Object ejbInstance = dependentSFSBToBeRemoved.remove(System.identityHashCode(instance));
        if (ejbInstance != null) {
            destroyStatefulSessionBeanInstance((T) ejbInstance, cc);
        } else {
            destroyStatefulSessionBeanInstance(instance, cc);
        }
    }

    public void storeStatefulInstance(final Object proxy, final T instance) {
        dependentSFSBToBeRemoved.put(System.identityHashCode(proxy), instance);
    }

    public void initInternals() {
        final List<Class> classes = beanContext.getBusinessLocalInterfaces();
        final boolean noLocalInterface = classes.isEmpty();
        if (beanContext.getComponentType().isMessageDriven()) {
            homeLocalBean = null;
            home = null;
            remote = null;
        } else if (beanContext.isLocalbean() || (noLocalInterface && beanContext.getBusinessRemoteInterfaces().isEmpty() /*EJB2*/)) {
            homeLocalBean = beanContext.getBusinessLocalBeanHome();
            home = null;
            remote = null;
        } else if (!noLocalInterface) {
            final Class<?> mainInterface = classes.get(0);
            final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, classes);
            interfaces.addAll(beanContext.getBusinessRemoteInterfaces());
            home = beanContext.getBusinessLocalHome(interfaces, mainInterface);
            homeLocalBean = null;
            remote = null;
        } else {
            final Class<?> mainInterface = beanContext.getBusinessRemoteInterface();
            final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, beanContext.getBusinessRemoteInterfaces());
            remote = beanContext.getBusinessRemoteHome(interfaces, mainInterface);
            home = null;
            homeLocalBean = null;
        }
    }

    public static class EJBBeanAttributesImpl<T> extends BeanAttributesImpl<T> { // TODO: move it in its own class
        private final BeanContext beanContext;
        private final Set<Type> ejbTypes;

        public EJBBeanAttributesImpl(final BeanContext bc, final BeanAttributes<T> beanAttributes) {
            super(beanAttributes);
            this.beanContext = bc;
            this.ejbTypes = new HashSet<>();
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
                final Map<Class<?>, Type> apis = new HashMap<>(cl.size());
                for (final Type t : beanContext.getManagedClass().getGenericInterfaces()) {
                    if (ParameterizedType.class.isInstance(t)) {
                        try {
                            apis.put(Class.class.cast(ParameterizedType.class.cast(t).getRawType()), t);
                        } catch (final Throwable th) {
                            // no-op
                        }
                    }
                }
                for (final Class<?> c : cl) {
                    final Type type = apis.get(c);
                    ejbTypes.addAll(GenericsUtil.getTypeClosure(type != null ? type : c));
                }
            }

            /* not in EJB types - 3.2.2 of cdi 1.2
            final List<Class> clRemote = beanContext.getBusinessRemoteInterfaces();
            if (clRemote != null && !clRemote.isEmpty()) {
                for (final Class<?> c : clRemote) {
                    ejbTypes.add(c); // parentInterfaces(c), but is it useful in practise?
                }
            }
            */

            ejbTypes.add(Object.class);
        }

        private static void addApiTypes(final Collection<Type> clazzes, final Class<?> beanClass) {
            final Typed typed = beanClass.getAnnotation(Typed.class);
            if (typed == null || typed.value().length == 0) {
                Type current = beanClass;
                while (current != null && Object.class != current) {
                    clazzes.add(current);
                    // TODO: better loop
                    current = Class.class.isInstance(current) ? Class.class.cast(current).getGenericSuperclass() : null;
                }
            } else {
                Collections.addAll(clazzes, typed.value());
            }
        }
    }

    public static class EjbInjectionTargetFactory<T> extends InjectionTargetFactoryImpl<T> {
        private final BeanContext beanContext;

        public EjbInjectionTargetFactory(final BeanContext bc, final AnnotatedType<T> annotatedType, final WebBeansContext webBeansContext) {
            super(annotatedType, webBeansContext);
            this.beanContext = bc;
        }

        @Override
        public InjectionTarget<T> createInjectionTarget(final Bean<T> bean) {
            final EjbInjectionTargetImpl<T> injectionTarget = new EjbInjectionTargetImpl<>(getAnnotatedType(), createInjectionPoints(bean), getWebBeansContext());
            final InjectionTarget<T> it = getWebBeansContext().getWebBeansUtil().fireProcessInjectionTargetEvent(injectionTarget, getAnnotatedType()).getInjectionTarget();

            for (final InjectionPoint ip : it.getInjectionPoints()) {
                if (ip.getType() != UserTransaction.class) {
                    continue;
                }
                if (beanContext.getTransactionType() != TransactionType.BeanManaged) {
                    throw new DefinitionException("@Inject UserTransaction is only valid for BeanManaged beans");
                }
            }

            if (!EjbInjectionTargetImpl.class.isInstance(it)) {
                return new EjbInjectionTargetImpl<>(injectionTarget, it);
            }
            return it;
        }

        @Override
        protected List<AnnotatedMethod<?>> getPostConstructMethods() {
            return Collections.emptyList();
        }

        @Override
        protected List<AnnotatedMethod<?>> getPreDestroyMethods() {
            return Collections.emptyList();
        }
    }

    public static class EjbInjectionTargetImpl<T> extends InjectionTargetImpl<T> {
        private CdiEjbBean<T> bean;
        private InjectionTarget<T> delegate;

        public EjbInjectionTargetImpl(final AnnotatedType<T> annotatedType, final Set<InjectionPoint> points, final WebBeansContext webBeansContext) {
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
        protected void defineLifecycleInterceptors(final Bean<T> bean, final AnnotatedType<T> annotatedType, final WebBeansContext webBeansContext) {
            if (!isDynamicBean(bean)) {
                super.defineLifecycleInterceptors(bean, annotatedType, webBeansContext);
            }
        }

        @Override
        public void defineInterceptorStack(final Bean<T> bean, final AnnotatedType<T> annotatedType, final WebBeansContext webBeansContext) {
            super.defineInterceptorStack(bean,
                    isDynamicBean(bean) ?
                            (AnnotatedType<T>) webBeansContext.getAnnotatedElementFactory()
                                    .newAnnotatedType(CdiEjbBean.class.cast(bean).getBeanContext().getManagedClass()) : annotatedType,
                    webBeansContext);
        }

        @Override
        protected boolean needsProxy() {
            return !bean.beanContext.isDynamicallyImplemented() && super.needsProxy();
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
            final CreationalContextImpl<T> ccImpl = CreationalContextImpl.class.cast(creationalContext);
            // super.produce(cc) will not work since we need the unproxied instance - decorator case
            final Map<javax.enterprise.inject.spi.Interceptor<?>, Object> interceptorInstances
                    = webBeansContext.getInterceptorResolutionService().createInterceptorInstances(getInterceptorInfo(), ccImpl);
            final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = super.getInterceptorInfo();
            if (interceptorInfo != null) {
                final Map<Constructor<?>, InterceptorResolutionService.BusinessMethodInterceptorInfo> constructorInterceptorInfos =
                        interceptorInfo.getConstructorInterceptorInfos();
                if (!constructorInterceptorInfos.isEmpty()) { // were missed by OWB
                    final javax.enterprise.inject.spi.Interceptor<?>[] ejbInterceptors = constructorInterceptorInfos.values().iterator().next().getEjbInterceptors();

                    if (null != ejbInterceptors) {
                        for (final javax.enterprise.inject.spi.Interceptor interceptorBean : ejbInterceptors) {
                            if (!interceptorInstances.containsKey(interceptorBean)) {
                                ccImpl.putContextual(interceptorBean);
                                interceptorInstances.put(interceptorBean, interceptorBean.create(ccImpl));
                            }
                        }
                    }
                }
            }
            final T produce = super.produce(interceptorInstances, ccImpl);
            if (produce == null) { // user didnt call ic.proceed() in @AroundConstruct
                return super.newInstance(ccImpl);
            }
            return (T) produce;
        }

        private static boolean isDynamicBean(final Bean<?> bean) {
            return CdiEjbBean.class.isInstance(bean) && CdiEjbBean.class.cast(bean).beanContext.isDynamicallyImplemented();
        }
    }
}
