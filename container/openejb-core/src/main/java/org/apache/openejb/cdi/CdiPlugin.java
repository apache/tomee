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
import org.apache.openejb.OpenEJBException;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.util.WebBeansUtil;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


public class CdiPlugin extends AbstractOwbPlugin implements OpenWebBeansJavaEEPlugin, OpenWebBeansEjbPlugin {

    private Set<Class<?>> beans;

    private WebBeansContext webBeansContext;
    private CdiAppContextsService contexsServices;
    private ClassLoader classLoader;

    private final Map<Contextual<?>, Object> cacheProxies = new ConcurrentHashMap<Contextual<?>, Object>();

    public void setWebBeansContext(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // this plugin may have been installed in a non-ejb lifecycle???
        if (beans != null) {
            this.beans.clear();
        }
    }

    public void configureDeployments(List<BeanContext> ejbDeployments) {
        WeakHashMap<Class<?>, Object> beans = new WeakHashMap<Class<?>, Object>();
        for (BeanContext deployment : ejbDeployments) {
            if (deployment.getComponentType().isSession()) {
                beans.put(deployment.getBeanClass(), null);
            }
        }
        this.beans = beans.keySet();
    }

    public CdiAppContextsService getContexsServices() {
        return contexsServices;
    }

    public void startup() {
        this.contexsServices = (CdiAppContextsService) webBeansContext.getContextsService();
        this.contexsServices.init(null);
    }

    public void stop() throws OpenEJBException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // Setting context class loader for cleaning
            Thread.currentThread().setContextClassLoader(classLoader);

            // Fire shut down
            webBeansContext.getBeanManagerImpl().fireEvent(new BeforeShutdownImpl());

            // Destroys context
            this.contexsServices.destroy(null);

            // Free all plugin resources
            webBeansContext.getPluginLoader().shutDown();

            // Clear extensions
            webBeansContext.getExtensionLoader().clear();

            // Delete Resolutions Cache
            webBeansContext.getBeanManagerImpl().getInjectionResolver().clearCaches();

            // Delte proxies
            webBeansContext.getProxyFactory().clear();

            // Delete AnnotateTypeCache
            webBeansContext.getAnnotatedElementFactory().clear();

            // Clear the resource injection service
            CdiResourceInjectionService injectionServices = (CdiResourceInjectionService) webBeansContext.getService(ResourceInjectionService.class);
            injectionServices.clear();

            // Clear singleton list
            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

        } catch (Exception e) {
            throw new OpenEJBException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    @Override
    public <T> T getSupportedService(Class<T> serviceClass) {
        return supportService(serviceClass) ? serviceClass.cast(this) : null;
    }

    @Override
    public void isManagedBean(Class<?> clazz) {
    }

    @Override
    public boolean supportService(Class<?> serviceClass) {
        return serviceClass == TransactionService.class || serviceClass == SecurityService.class;
    }

    @Override
    public Object getSessionBeanProxy(Bean<?> inBean, Class<?> interfce, CreationalContext<?> creationalContext) {
        Object instance = cacheProxies.get(inBean);
        if (instance != null) {
            return instance;
        }

        final Class<? extends Annotation> scopeClass = inBean.getScope();
        final CdiEjbBean<Object> cdiEjbBean = (CdiEjbBean<Object>) inBean;
        final CreationalContext<Object> cc = (CreationalContext<Object>) creationalContext;

        if (scopeClass == null || Dependent.class == scopeClass) { // no need to add any layer, null = @New
            return cdiEjbBean.createEjb(cc);
        }

        // only stateful normally
        final InstanceBean<Object> bean = new InstanceBean<Object>(cdiEjbBean);
        if (webBeansContext.getBeanManagerImpl().isScopeTypeNormal(scopeClass)) {
            instance = webBeansContext.getProxyFactory().createNormalScopedBeanProxy(bean, creationalContext);
            cacheProxies.put(inBean, instance);
        } else {
            final Context context = webBeansContext.getBeanManagerImpl().getContext(scopeClass);
            instance = context.get(bean, cc);
        }
        bean.setOWBProxy(instance);
        return instance;
    }

    @Override
    public boolean isSessionBean(Class<?> clazz) {
        // this may be called from a web app without ejbs in which case beans will not have been initialized by openejb.
        return beans != null && beans.contains(clazz);
    }

    @Override
    public <T> Bean<T> defineSessionBean(Class<T> clazz, ProcessAnnotatedType<T> processAnnotateTypeEvent) {
        throw new IllegalStateException("Statement should never be reached");
    }

    @Override
    public boolean isSingletonBean(Class<?> clazz) {
        throw new IllegalStateException("Statement should never be reached");
    }

    @Override
    public boolean isStatefulBean(Class<?> clazz) {
        // TODO Make the EjbPlugin pass in the Bean<T> instance
        return clazz.isAnnotationPresent(Stateful.class);
    }

    @Override
    public boolean isStatelessBean(Class<?> clazz) {
        throw new IllegalStateException("Statement should never be reached");
    }

    @Override
    public Method resolveViewMethod(Bean<?> component, Method declaredMethod) {
        if (!(component instanceof CdiEjbBean)) return declaredMethod;

        CdiEjbBean cdiEjbBean = (CdiEjbBean) component;

        final BeanContext beanContext = cdiEjbBean.getBeanContext();

        for (Class intface : beanContext.getBusinessLocalInterfaces()) {
            try {
                return intface.getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
            } catch (NoSuchMethodException ignore) {
            }
        }
        return declaredMethod;
    }

    //TODO Delete if we end up not needing this
    public Method resolveBeanMethod(Bean<?> component, Method declaredMethod) {
        if (!(component instanceof CdiEjbBean)) return declaredMethod;

        CdiEjbBean cdiEjbBean = (CdiEjbBean) component;

        final BeanContext beanContext = cdiEjbBean.getBeanContext();

        try {
            return beanContext.getBeanClass().getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return declaredMethod;
        }
    }

    public void clearProxies() {
        cacheProxies.clear();
    }

    // does pretty much nothing
    // used only to get a layer between our EJB proxies and OWB proxies to let them manage the scope
    private static class InstanceBean<T> implements OwbBean<T>, PassivationCapable {
        private final CdiEjbBean<T> bean;
        private T OWBProxy;

        public InstanceBean(final CdiEjbBean<T> tCdiEjbBean) {
            bean = tCdiEjbBean;
        }

        @Override
        public T createNewInstance(final CreationalContext<T> creationalContext) {
            return create(creationalContext);
        }

        @Override
        public void destroyCreatedInstance(final T instance, final CreationalContext<T> creationalContext) {
            bean.destroyComponentInstance(instance, creationalContext);
        }

        @Override
        public Set<Type> getTypes() {
            return bean.getTypes();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return bean.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return bean.getScope();
        }

        @Override
        public String getName() {
            return bean.getName();
        }

        @Override
        public boolean isNullable() {
            return bean.isNullable();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return bean.getBeanClass();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return bean.getStereotypes();
        }

        @Override
        public boolean isAlternative() {
            return bean.isAlternative();
        }

        @Override
        public T create(final CreationalContext<T> creationalContext) {
            final T instance = bean.createEjb(creationalContext);
            if (OWBProxy != null && SessionBeanType.STATEFUL.equals(bean.getEjbType())) { // we need to be able to remove OWB proxy to remove (statefuls for instance)
                bean.storeStatefulInstance(OWBProxy, instance);
            }
            return instance;
        }

        @Override
        public void destroy(final T instance, final CreationalContext<T> cc) {
            if (!SessionBeanType.STATEFUL.equals(bean.getEjbType())) {
                return;
            }

            bean.destroy(instance, cc);
        }

        @Override
        public void setImplScopeType(final Annotation scopeType) {
            // no-op
        }

        @Override
        public WebBeansType getWebBeansType() {
            return bean.getWebBeansType();
        }

        @Override
        public void addQualifier(final Annotation qualifier) {
            // no-op
        }

        @Override
        public boolean isSerializable() {
            return bean.isSerializable();
        }

        @Override
        public void addStereoType(final Annotation stereoType) {
            // no-op
        }

        @Override
        public void addApiType(final Class<?> apiType) {
            // no-op
        }

        @Override
        public void addInjectionPoint(final InjectionPoint injectionPoint) {
            // no-op
        }

        @Override
        public Set<Annotation> getOwbStereotypes() {
            return bean.getOwbStereotypes();
        }

        @Override
        public void setName(final String name) {
            // no-op
        }

        @Override
        public List<InjectionPoint> getInjectionPoint(final Member member) {
            return Collections.emptyList();
        }

        @Override
        public Class<T> getReturnType() {
            return bean.getReturnType();
        }

        @Override
        public void setSerializable(final boolean serializable) {
            // no-op
        }

        @Override
        public void setNullable(final boolean nullable) {
            // no-op
        }

        @Override
        public void setSpecializedBean(boolean specialized) {
            // no-op
        }

        @Override
        public boolean isSpecializedBean() {
            return bean.isSpecializedBean();
        }

        @Override
        public void setEnabled(boolean enabled) {
            // no-op
        }

        @Override
        public boolean isEnabled() {
            return bean.isEnabled();
        }

        @Override
        public String getId() {
            return bean.getId();
        }

        @Override
        public boolean isPassivationCapable() {
            return bean.isPassivationCapable();
        }

        @Override
        public boolean isDependent() {
            return bean.isDependent();
        }

        @Override
        public void validatePassivationDependencies() {
            bean.validatePassivationDependencies();
        }

        @Override
        public WebBeansContext getWebBeansContext() {
            return bean.getWebBeansContext();
        }

        public void setOWBProxy(final T OWBProxy) {
            this.OWBProxy = OWBProxy;
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || bean == o;
        }

        @Override
        public int hashCode() {
            return bean.hashCode();
        }
    }
}
