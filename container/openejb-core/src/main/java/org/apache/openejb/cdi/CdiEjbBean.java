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
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

import javax.ejb.NoSuchEJBException;
import javax.ejb.Remove;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.SessionBeanType;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.NoSuchObjectException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CdiEjbBean<T> extends BaseEjbBean<T> {
    private final Map<Integer, Object> dependentSFSBToBeRemoved = new ConcurrentHashMap<Integer, Object>();

    private final BeanContext beanContext;

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext) {
        this(beanContext, webBeansContext, beanContext.getManagedClass());
    }

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext, Class beanClass) {
        super(beanClass, toSessionType(beanContext.getComponentType()), webBeansContext);
        this.beanContext = beanContext;
        beanContext.set(Bean.class, this);
    }

    @Override // copied to be able to produce EM (should be fixed in OWB for next CDI spec)
    public void validatePassivationDependencies() {
        if(isPassivationCapable()) {
            final Set<InjectionPoint> beanInjectionPoints = getInjectionPoints();
            for(InjectionPoint injectionPoint : beanInjectionPoints) {
                if(!injectionPoint.isTransient()) {
                    if(!getWebBeansContext().getWebBeansUtil().isPassivationCapableDependency(injectionPoint)) {
                        if(injectionPoint.getAnnotated().isAnnotationPresent(Disposes.class)
                                // here is the hack, this is temporary until OWB manages correctly serializable instances
                                || EntityManager.class.equals(injectionPoint.getAnnotated().getBaseType())) {
                            continue;
                        }
                        throw new WebBeansConfigurationException(
                                "Passivation capable beans must satisfy passivation capable dependencies. " +
                                        "Bean : " + toString() + " does not satisfy. Details about the Injection-point: " +
                                        injectionPoint.toString());
                    }
                }
            }
        }

        //Check for interceptors and decorators, copied from parent(s)
        for (Decorator<?> dec : decorators) {
            WebBeansDecorator<?> decorator = (WebBeansDecorator<?>) dec;
            if (!decorator.isPassivationCapable()) {
                throw new WebBeansConfigurationException(MessageFormat.format(
                        WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0015), toString()));
            } else {
                decorator.validatePassivationDependencies();
            }
        }

        for (InterceptorData interceptorData : interceptorStack) {
            if (interceptorData.isDefinedWithWebBeansInterceptor()) {
                WebBeansInterceptor<?> interceptor = (WebBeansInterceptor<?>) interceptorData.getWebBeansInterceptor();
                if (!interceptor.isPassivationCapable()) {
                    throw new WebBeansConfigurationException(MessageFormat.format(
                            WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0016), toString()));
                } else {
                    interceptor.validatePassivationDependencies();
                }
            } else {
                if (interceptorData.isDefinedInInterceptorClass()) {
                    Class<?> interceptorClass = interceptorData.getInterceptorClass();
                    if (!Serializable.class.isAssignableFrom(interceptorClass)) {
                        throw new WebBeansConfigurationException(MessageFormat.format(
                                WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0016), toString()));
                    } else {
                        if (!getWebBeansContext().getAnnotationManager().checkInjectionPointForInterceptorPassivation(interceptorClass)) {
                            throw new WebBeansConfigurationException(MessageFormat.format(
                                    WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0017), toString(), interceptorClass));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addApiType(final Class<?> apiType) {
        if (apiType == null) return;

        super.addApiType(apiType);
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
            return SessionBeanType.STATELESS;
        case STATEFUL:
        case MANAGED:
            return SessionBeanType.STATEFUL;
        default:
            throw new IllegalStateException("Unknown Session BeanType " + beanType);
        }
    }

    @Override
    public String getId() {
        return beanContext.getDeploymentID() + getReturnType().getName();
    }

    @Override
    protected void afterConstructor(T instance, CreationalContext<T> tCreationalContext) {
        // no-op
    }

//    @Override
//    public void postConstruct(T instance, CreationalContext<T> cretionalContext) {
//    }

    public String getEjbName() {
        return this.beanContext.getEjbName();
    }

    public boolean needsBeanLocalViewAddedToTypes() {
        return beanContext.isLocalbean() && beanContext.getBeanClass().getAnnotation(Typed.class) == null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<?>> getBusinessLocalInterfaces() {
        final List<Class<?>> clazzes = new ArrayList<Class<?>>();

        if (beanContext.isLocalbean()) {
            addApiTypes(clazzes, beanContext.getBeanClass());
        }

        if (beanContext.getProxyClass() != null) {
            addApiTypes(clazzes, beanContext.getProxyClass());
        }

        final List<Class> cl = beanContext.getBusinessLocalInterfaces();
        if (cl != null && !cl.isEmpty()) {
            for (Class<?> c : cl) {
                clazzes.add(c);
            }
        }

        if (!clazzes.contains(Serializable.class)) {
            clazzes.add(Serializable.class);
        }

        return clazzes;
    }

    private static void addApiTypes(final List<Class<?>> clazzes, final Class<?> beanClass) {
        final Typed typed = beanClass.getAnnotation(Typed.class);
        if (typed == null || typed.value().length == 0) {
            clazzes.add(beanClass);
        } else {
            for (Class<?> clazz : typed.value()) {
                clazzes.add(clazz);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getInstance(final CreationalContext<T> creationalContext) {
        return createEjb(creationalContext);

        /*
        final T instance;
        if (scopeClass == null || Dependent.class == scopeClass) { // no need to add any layer, null = @New
            instance = createEjb(creationalContext);
        } else { // only stateful normally
            final InstanceBean<T> bean = new InstanceBean<T>(this);
            if (webBeansContext.getBeanManagerImpl().isScopeTypeNormal(scopeClass)) {
                instance = (T) webBeansContext.getProxyFactory().createNormalScopedBeanProxy(bean, creationalContext);
            } else {
                final Context context = webBeansContext.getBeanManagerImpl().getContext(scopeClass);
                instance = context.get(bean, creationalContext);
            }
            bean.setOWBProxy(instance);
        }
        return instance;
        */
    }

    @Override
    protected void destroyComponentInstance(final T instance, final CreationalContext<T> creational) {
        if (scopeClass == null || Dependent.class == scopeClass) {
            destroyStatefulSessionBeanInstance(instance, creational);
        } else {
            destroyScopedStateful(instance, creational);
        }
    }

    @Override
    protected void destroyStatefulSessionBeanInstance(final T proxyInstance, final Object ejbInstance) {
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

    public List<InjectionPoint> getInjectionPoint(Member member)
    {
        if (member instanceof Method) {
            Method method = (Method) member;
            member = beanContext.getMatchingBeanMethod(method);
        }

        List<InjectionPoint> points = new ArrayList<InjectionPoint>();

        for(InjectionPoint ip : injectionPoints)
        {
            if(ip.getMember().equals(member))
            {
                points.add(ip);
            }
        }

        return points;
    }

    protected void specialize(CdiEjbBean<?> superBean) {
        final CdiEjbBean<T> bean = this;
        bean.setName(superBean.getName());
        bean.setSerializable(superBean.isSerializable());

        this.scopeClass = superBean.scopeClass;
        this.implQualifiers.addAll(superBean.getQualifiers());
        this.stereoTypeClasses.addAll(superBean.stereoTypeClasses);
        this.stereoTypes.addAll(superBean.stereoTypes);
    }

    /* (non-Javadoc)
     * @see org.apache.webbeans.component.AbstractBean#isPassivationCapable()
     */
    @Override
    public boolean isPassivationCapable() {
        return getWebBeansContext().getBeanManagerImpl().isPassivatingScope(getScope());
    }

    @SuppressWarnings("unchecked")
    private List<Method> findRemove(Class beanClass, Class beanInterface) {
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
                    interfaceMethod = beanInterface.getMethod(method.getName(), method
                            .getParameterTypes());

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
            if (classes.size() == 0 && beanContext.isLocalbean()) {
                final BeanContext.BusinessLocalBeanHome home = beanContext.getBusinessLocalBeanHome();
                return (T) home.create();
            } else {
                final Class<?> mainInterface = classes.get(0);
                final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, classes);
                final BeanContext.BusinessLocalHome home = beanContext.getBusinessLocalHome(interfaces, mainInterface);
                return (T) home.create();
            }
        } finally {
            currentCreationalContext.set(existing);
        }
    }

    private void destroyScopedStateful(final T instance, final CreationalContext<T> cc) {
        try {
            instance.hashCode(); // force the instance to be created - otherwise we'll miss @PreDestroy for instance
        } catch (NoSuchEJBException e) {
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
}
