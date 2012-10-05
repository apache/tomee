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
import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;

import javax.ejb.Remove;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CdiEjbBean<T> extends BaseEjbBean<T> {
    private final BeanContext beanContext;

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext) {
        this(beanContext, webBeansContext, beanContext.getManagedClass());
    }

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext, Class beanClass) {
        super(beanClass, toSessionType(beanContext.getComponentType()), webBeansContext);
        this.beanContext = beanContext;
        beanContext.set(Bean.class, this);
    }

    @Override
    public void addApiType(Class<?> apiType) {
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
    @SuppressWarnings("unchecked")
    protected T getInstance(CreationalContext<T> creationalContext) {
        return (T) webBeansContext.getBeanManagerImpl().getReference(new RealEjbBean(this), getBeanClass(), creationalContext);
    }

    @Override
    protected void afterConstructor(T instance, CreationalContext<T> tCreationalContext) {
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
        List<Class<?>> clazzes = new ArrayList<Class<?>>();

        if (beanContext.isLocalbean()) {
            addApiTypes(clazzes, beanContext.getBeanClass());
        } else if (beanContext.getProxyClass() != null) {
            addApiTypes(clazzes, beanContext.getProxyClass());
        } else {
            List<Class> cl = beanContext.getBusinessLocalInterfaces();

            if (cl != null && !cl.isEmpty()) {
                for (Class<?> c : cl) {
                    clazzes.add(c);
                }
            }
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
    protected void destroyComponentInstance(T instance, CreationalContext<T> creational) {

        if (instance instanceof BeanContext.Removable) {
            BeanContext.Removable removable = (BeanContext.Removable) instance;
            removable.$$remove();
        }

    }

    @Override
    protected void destroyStatefulSessionBeanInstance(T proxyInstance, Object ejbInstance) {
        // super.destroyStatefulSessionBeanInstance(proxyInstance, ejbInstance);
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

    private static class RealEjbBean<T> extends AbstractOwbBean<T> {
        private final CdiEjbBean<T> delegate;

        public RealEjbBean(final CdiEjbBean<T> cdiEjbBean) {
            super(cdiEjbBean.getWebBeansType(), cdiEjbBean.getReturnType(), cdiEjbBean.getWebBeansContext());

            delegate = cdiEjbBean;

            setNullable(cdiEjbBean.isNullable());
            setSerializable(cdiEjbBean.isSerializable());
            setEnabled(cdiEjbBean.isEnabled());
            setName(cdiEjbBean.getName());
            setSpecializedBean(cdiEjbBean.isSpecializedBean());
        }

        @Override
        public void setImplScopeType(final Annotation scopeType) {
            delegate.setImplScopeType(scopeType);
        }


        @Override
        public T createNewInstance(final CreationalContext<T> creationalContext) {
            final List<Class> classes = delegate.beanContext.getBusinessLocalInterfaces();
            CurrentCreationalContext currentCreationalContext = delegate.beanContext.get(CurrentCreationalContext.class);
            CreationalContext existing = currentCreationalContext.get();
            currentCreationalContext.set(creationalContext);
            try {
                if (classes.size() == 0 && delegate.beanContext.isLocalbean()) {
                    BeanContext.BusinessLocalBeanHome home = delegate.beanContext.getBusinessLocalBeanHome();
                    return (T) home.create();
                } else {
                    final Class<?> mainInterface = classes.get(0);
                    List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(delegate.beanContext.getBeanClass(), mainInterface, classes);
                    BeanContext.BusinessLocalHome home = delegate.beanContext.getBusinessLocalHome(interfaces, mainInterface);
                    return (T) home.create();
                }
            } finally {
                currentCreationalContext.set(existing);
            }
        }

        @Override
        public void destroyCreatedInstance(final T instance, final CreationalContext<T> creationalContext) {
            delegate.destroyCreatedInstance(instance, creationalContext);
        }

        @Override
        public void addQualifier(final Annotation qualifier) {
            delegate.addQualifier(qualifier);
        }

        @Override
        public void addStereoType(final Annotation stereoType) {
            delegate.addStereoType(stereoType);
        }

        @Override
        public void addApiType(final Class<?> apiType) {
            delegate.addApiType(apiType);
        }

        @Override
        public void addInjectionPoint(final InjectionPoint injectionPoint) {
            delegate.addInjectionPoint(injectionPoint);
        }

        @Override
        public Set<Annotation> getOwbStereotypes() {
            return delegate.getOwbStereotypes();
        }

        @Override
        public List<InjectionPoint> getInjectionPoint(final Member member) {
            return delegate.getInjectionPoint(member);
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public boolean isPassivationCapable() {
            return delegate.isPassivationCapable();
        }

        @Override
        public boolean isDependent() {
            return delegate.isDependent();
        }

        @Override
        public void validatePassivationDependencies() {
            delegate.validatePassivationDependencies();
        }

        @Override
        public WebBeansContext getWebBeansContext() {
            return delegate.getWebBeansContext();
        }

        @Override
        public Set<Type> getTypes() {
            return delegate.getTypes();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return delegate.getScope();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return delegate.getInjectionPoints();
        }

        @Override
        public Class<?> getBeanClass() {
            return delegate.getBeanClass();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }

        @Override
        protected T createInstance(final CreationalContext<T> creationalContext) {
            return createNewInstance(creationalContext);
        }

        @Override
        protected void destroyInstance(final T instance, final CreationalContext<T> creationalContext) {
            delegate.destroyCreatedInstance(instance, creationalContext);
        }
    }
}
