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
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.ProducerFieldBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.component.creation.ObserverMethodsBuilder;
import org.apache.webbeans.component.creation.ProducerFieldBeansBuilder;
import org.apache.webbeans.component.creation.ProducerMethodBeansBuilder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.event.ObserverMethodImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.portable.events.generics.GProcessSessionBean;
import org.apache.webbeans.proxy.NormalScopeProxyFactory;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.inject.spi.SessionBeanType;
import javax.inject.Provider;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


public class CdiPlugin extends AbstractOwbPlugin implements OpenWebBeansJavaEEPlugin, OpenWebBeansEjbPlugin {

    private Map<Class<?>, BeanContext> beans;

    private WebBeansContext webBeansContext;
    private CdiAppContextsService contexsServices;
    private ClassLoader classLoader;

    private Map<Contextual<?>, Object> cacheProxies;

    public void setWebBeansContext(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
        if (!WebappWebBeansContext.class.isInstance(webBeansContext)) {
            cacheProxies = new ConcurrentHashMap<Contextual<?>, Object>();
        } else { // share cache of proxies between the whole app otherwise hard to share an EJB between a webapp and the lib part of the app
            cacheProxies = CdiPlugin.class.cast(WebappWebBeansContext.class.cast(webBeansContext).getParent().getPluginLoader().getEjbPlugin()).cacheProxies;
        }
    }

    public void setClassLoader(final ClassLoader classLoader) {
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

    public void configureDeployments(final List<BeanContext> ejbDeployments) {
        beans = new WeakHashMap<Class<?>, BeanContext>();
        for (final BeanContext deployment : ejbDeployments) {
            if (deployment.getComponentType().isSession()) {
                if (deployment.isLocalbean() && !deployment.isDynamicallyImplemented()) {
                    beans.put(deployment.get(BeanContext.ProxyClass.class).getProxy(), deployment);
                }
                beans.put(deployment.getBeanClass(), deployment);
            }
        }
    }

    public CdiAppContextsService getContexsServices() {
        return contexsServices;
    }

    public void startup() {
        this.contexsServices = (CdiAppContextsService) webBeansContext.getContextsService();
        this.contexsServices.init(null);
    }

    public void stop() throws OpenEJBException {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
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

            // Delete AnnotateTypeCache
            webBeansContext.getAnnotatedElementFactory().clear();

            // Clear the resource injection service
            final CdiResourceInjectionService injectionServices = (CdiResourceInjectionService) webBeansContext.getService(ResourceInjectionService.class);
            injectionServices.clear();

            // Clear singleton list
            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

        } catch (final Exception e) {
            throw new OpenEJBException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    @Override
    public <T> T getSupportedService(final Class<T> serviceClass) {
        return supportService(serviceClass) ? serviceClass.cast(this) : null;
    }

    @Override
    public void isManagedBean(final Class<?> clazz) {
    }

    @Override
    public boolean supportService(final Class<?> serviceClass) {
        return serviceClass == TransactionService.class || serviceClass == SecurityService.class;
    }

    @Override
    public Object getSessionBeanProxy(final Bean<?> inBean, final Class<?> interfce, final CreationalContext<?> creationalContext) {
        Object instance = cacheProxies.get(inBean);
        if (instance != null) {
            return instance;
        }

        synchronized (inBean) { // singleton for the app so safe to sync on it
            instance = cacheProxies.get(inBean);
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
            if (webBeansContext.getBeanManagerImpl().isNormalScope(scopeClass)) {
                final BeanContext beanContext = cdiEjbBean.getBeanContext();
                final Provider provider = webBeansContext.getNormalScopeProxyFactory().getInstanceProvider(beanContext.getClassLoader(), cdiEjbBean);

                if (!beanContext.isLocalbean()) {
                    final List<Class> interfaces = new ArrayList<Class>();
                    final InterfaceType type = beanContext.getInterfaceType(interfce);
                    if (type != null) {
                        interfaces.addAll(beanContext.getInterfaces(type));
                    } else { // can happen when looked up from impl instead of API in OWB -> default to business local
                        interfaces.addAll(beanContext.getInterfaces(InterfaceType.BUSINESS_LOCAL));
                    }
                    interfaces.add(Serializable.class);
                    interfaces.add(IntraVmProxy.class);
                    if (BeanType.STATEFUL.equals(beanContext.getComponentType()) || BeanType.MANAGED.equals(beanContext.getComponentType())) {
                        interfaces.add(BeanContext.Removable.class);
                    }

                    try {
                        instance = ProxyManager.newProxyInstance(interfaces.toArray(new Class<?>[interfaces.size()]), new InvocationHandler() {
                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                try {
                                    return method.invoke(provider.get(), args);
                                } catch (final InvocationTargetException ite) {
                                    throw ite.getCause();
                                }
                            }
                        });
                    } catch (final IllegalAccessException e) {
                        throw new OpenEJBRuntimeException(e);
                    }

                } else {
                    final NormalScopeProxyFactory normalScopeProxyFactory = webBeansContext.getNormalScopeProxyFactory();
                    final Class<?> proxyClass = normalScopeProxyFactory.createProxyClass(beanContext.getClassLoader(), beanContext.getBeanClass());
                    instance = normalScopeProxyFactory.createProxyInstance(proxyClass, provider);
                }

                cacheProxies.put(inBean, instance);
            } else {
                final Context context = webBeansContext.getBeanManagerImpl().getContext(scopeClass);
                instance = context.get(bean, cc);
            }
            bean.setOwbProxy(instance);
            return instance;
        }
    }

    @Override
    public boolean isSessionBean(final Class<?> clazz) {
        // this may be called from a web app without ejbs in which case beans will not have been initialized by openejb.
        return beans != null && beans.containsKey(clazz);
    }

    @Override
    public boolean isNewSessionBean(final Class<?> clazz) {
        // this may be called from a web app without ejbs in which case beans will not have been initialized by openejb.
        return isNewSessionBean(webBeansContext, clazz) || isNewSessionBean(superContext(webBeansContext), clazz);
    }

    private boolean isNewSessionBean(final WebBeansContext ctx, final Class<?> clazz) {
        if (ctx == null) {
            return false;
        }

        final Map<Class<?>, BeanContext> map = pluginBeans(ctx);
        return map != null && (map.containsKey(clazz) || clazz.isInterface() && findBeanContext(ctx, clazz) != null);
    }

    private static WebBeansContext superContext(final WebBeansContext ctx) {
        if (!WebappWebBeansContext.class.isInstance(ctx)) {
            return null;
        }
        return WebappWebBeansContext.class.cast(ctx).getParent();
    }

    private static BeanContext findBeanContext(final WebBeansContext ctx, final Class<?> clazz) {
        final Map<Class<?>, BeanContext> beans = pluginBeans(ctx);

        final BeanContext b = beans.get(clazz);
        if (b != null) {
            return b;
        }

        for (final BeanContext bc : beans.values()) {
            if (bc.isLocalbean()) {
                continue; // see isSessionBean() impl
            }

            final CdiEjbBean<?> cdiEjbBean = bc.get(CdiEjbBean.class);
            if (cdiEjbBean == null) {
                continue;
            }

            for (final Class<?> itf : cdiEjbBean.getBusinessLocalInterfaces()) {
                if (itf.equals(clazz)) {
                    return bc;
                }
            }
        }

        final WebBeansContext parentCtx = superContext(ctx);
        if (parentCtx != null) {
            return findBeanContext(parentCtx, clazz);
        }

        return null;
    }

    @Override
    public <T> Bean<T> defineSessionBean(final Class<T> clazz, final ProcessAnnotatedType<T> processAnnotateTypeEvent) {
        final BeanContext bc = findBeanContext(webBeansContext, clazz);
        final AnnotatedType<T> annotatedType = processAnnotateTypeEvent.getAnnotatedType();
        final CdiEjbBean<T> bean = new OpenEJBBeanBuilder<T>(bc, webBeansContext, annotatedType).createBean(clazz, isActivated(processAnnotateTypeEvent));

        bc.set(CdiEjbBean.class, bean);
        bc.set(CurrentCreationalContext.class, new CurrentCreationalContext());

        validateDisposeMethods(bean);
        validateScope(bean);

        final Set<ObserverMethod<?>> observerMethods;
        if (bean.isEnabled()) {
            observerMethods = new ObserverMethodsBuilder<T, InjectionTargetBean<T>>(webBeansContext, bean.getAnnotatedType()).defineObserverMethods(bean);
        } else {
            observerMethods = new HashSet<ObserverMethod<?>>();
        }

        final WebBeansUtil webBeansUtil = webBeansContext.getWebBeansUtil();

        final Set<ProducerMethodBean<?>> producerMethods = new ProducerMethodBeansBuilder(bean.getWebBeansContext(), bean.getAnnotatedType()).defineProducerMethods(bean);
        final Set<ProducerFieldBean<?>> producerFields = new ProducerFieldBeansBuilder(bean.getWebBeansContext(), bean.getAnnotatedType()).defineProducerFields(bean);

        final Map<ProducerMethodBean<?>, AnnotatedMethod<?>> annotatedMethods = new HashMap<ProducerMethodBean<?>, AnnotatedMethod<?>>();
        for (final ProducerMethodBean<?> producerMethod : producerMethods) {
            final AnnotatedMethod<?> method = webBeansContext.getAnnotatedElementFactory().newAnnotatedMethod(producerMethod.getCreatorMethod(), annotatedType);
            webBeansUtil.inspectErrorStack("There are errors that are added by ProcessProducer event observers for "
                + "ProducerMethods. Look at logs for further details");

            annotatedMethods.put(producerMethod, method);
        }

        final Map<ProducerFieldBean<?>, AnnotatedField<?>> annotatedFields = new HashMap<ProducerFieldBean<?>, AnnotatedField<?>>();
        for (final ProducerFieldBean<?> producerField : producerFields) {
            webBeansUtil.inspectErrorStack("There are errors that are added by ProcessProducer event observers for"
                + " ProducerFields. Look at logs for further details");

            annotatedFields.put(producerField,
                webBeansContext.getAnnotatedElementFactory().newAnnotatedField(
                    producerField.getCreatorField(),
                    webBeansContext.getAnnotatedElementFactory().newAnnotatedType(producerField.getBeanClass())));
        }

        final Map<ObserverMethod<?>, AnnotatedMethod<?>> observerMethodsMap = new HashMap<ObserverMethod<?>, AnnotatedMethod<?>>();
        for (final ObserverMethod<?> observerMethod : observerMethods) {
            final ObserverMethodImpl<?> impl = (ObserverMethodImpl<?>) observerMethod;
            final AnnotatedMethod<?> method = impl.getObserverMethod();

            observerMethodsMap.put(observerMethod, method);
        }

        validateProduceMethods(bean, producerMethods);
        validateObserverMethods(bean, observerMethodsMap);

        final BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();

        //Fires ProcessManagedBean
        beanManager.fireEvent(new GProcessSessionBean(Bean.class.cast(bean), annotatedType, bc.getEjbName(), bean.getEjbType()));
        webBeansUtil.inspectErrorStack("There are errors that are added by ProcessSessionBean event observers for managed beans. Look at logs for further details");

        //Fires ProcessProducerMethod
        webBeansUtil.fireProcessProducerMethodBeanEvent(annotatedMethods, annotatedType);
        webBeansUtil.inspectErrorStack("There are errors that are added by ProcessProducerMethod event observers for producer method beans. Look at logs for further details");

        //Fires ProcessProducerField
        webBeansUtil.fireProcessProducerFieldBeanEvent(annotatedFields);
        webBeansUtil.inspectErrorStack("There are errors that are added by ProcessProducerField event observers for producer field beans. Look at logs for further details");

        //Fire ObservableMethods
        webBeansUtil.fireProcessObservableMethodBeanEvent(observerMethodsMap);
        webBeansUtil.inspectErrorStack("There are errors that are added by ProcessObserverMethod event observers for observer methods. Look at logs for further details");

        if (!webBeansUtil.isAnnotatedTypeDecoratorOrInterceptor(annotatedType)) {
            for (final ProducerMethodBean<?> producerMethod : producerMethods) {
                beanManager.addBean(producerMethod);
            }
            for (final ProducerFieldBean<?> producerField : producerFields) {
                beanManager.addBean(producerField);
            }
        }

        beanManager.addBean(bean);

        return bean;
    }


    @Override
    public <T> Bean<T> defineNewSessionBean(final Class<T> clazz) {
        final NewCdiEjbBean<T> newBean = new NewCdiEjbBean<T>(findBeanContext(webBeansContext, clazz).get(CdiEjbBean.class));
        webBeansContext.getBeanManagerImpl().addBean(newBean);
        return newBean;
    }

    private static Map<Class<?>, BeanContext> pluginBeans(final WebBeansContext ctx) {
        return CdiPlugin.class.cast(ctx.getPluginLoader().getEjbPlugin()).beans;
    }

    private static void validateObserverMethods(final CdiEjbBean<?> bean, final Map<ObserverMethod<?>, AnnotatedMethod<?>> methods) {
        final BeanContext beanContext = bean.getBeanContext();
        if (beanContext.isLocalbean()) {
            return;
        }

        for (final Map.Entry<ObserverMethod<?>, AnnotatedMethod<?>> m : methods.entrySet()) {
            final Method method = m.getValue().getJavaMember();
            if (!Modifier.isStatic(method.getModifiers()) && doResolveViewMethod(bean, method) == null) {
                throw new WebBeansConfigurationException("@Observes " + method + " neither in the ejb view of ejb " + bean.getBeanContext().getEjbName() + " nor static");
            }
        }
    }

    private static void validateProduceMethods(final CdiEjbBean<?> bean, final Set<ProducerMethodBean<?>> methods) {
        final BeanContext beanContext = bean.getBeanContext();
        if (beanContext.isLocalbean()) {
            return;
        }

        for (final ProducerMethodBean<?> m : methods) {
            final Method method = m.getCreatorMethod();
            if (doResolveViewMethod(bean, method) == null) {
                throw new WebBeansConfigurationException("@Produces " + method + " not in the ejb view of ejb " + beanContext.getEjbName());
            }
        }
    }

    private static void validateScope(final CdiEjbBean<?> bean) {
        final Class<? extends Annotation> scope = bean.getScope();
        final BeanType beanType = bean.getBeanContext().getComponentType();

        if (BeanType.STATELESS.equals(beanType) && !Dependent.class.equals(scope)) {
            throw new WebBeansConfigurationException("@Stateless can only be @Dependent");
        }
        if (BeanType.SINGLETON.equals(beanType) && !Dependent.class.equals(scope) && !ApplicationScoped.class.equals(scope)) {
            throw new WebBeansConfigurationException("@Singleton can only be @Dependent or @ApplicationScoped");
        }
    }

    private static void validateDisposeMethods(final CdiEjbBean<?> bean) {
        if (!bean.getBeanContext().isLocalbean()) {
            for (final Method m : bean.getBeanContext().getBeanClass().getMethods()) {
                if (m.getDeclaringClass().equals(Object.class)) {
                    continue;
                }

                if (m.getParameterTypes().length > 0) {
                    for (final Annotation[] a : m.getParameterAnnotations()) {
                        for (final Annotation ann : a) {
                            if (ann.annotationType().equals(Disposes.class) && doResolveViewMethod(bean, m) == null) {
                                throw new WebBeansConfigurationException("@Disposes is forbidden on non business EJB methods");
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isActivated(final ProcessAnnotatedType<?> pat) {
        return !ProcessAnnotatedTypeImpl.class.isInstance(pat) || !ProcessAnnotatedTypeImpl.class.cast(pat).isVeto();
    }

    @Override
    public boolean isSingletonBean(final Class<?> clazz) {
        throw new IllegalStateException("Statement should never be reached");
    }

    @Override
    public boolean isStatefulBean(final Class<?> clazz) {
        return BeanType.STATEFUL.equals(beans.get(clazz).getComponentType());
    }

    @Override
    public boolean isStatelessBean(final Class<?> clazz) {
        throw new IllegalStateException("Statement should never be reached");
    }

    public static Method doResolveViewMethod(final Bean<?> component, final Method declaredMethod) {
        if (!(component instanceof CdiEjbBean)) {
            return declaredMethod;
        }

        final CdiEjbBean cdiEjbBean = (CdiEjbBean) component;

        final BeanContext beanContext = cdiEjbBean.getBeanContext();

        for (final Class intface : beanContext.getBusinessLocalInterfaces()) {
            try {
                return intface.getMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
            } catch (final NoSuchMethodException ignore) {
                // no-op
            }
        }
        return null;
    }

    @Override
    public Method resolveViewMethod(final Bean<?> component, final Method declaredMethod) {
        final Method m = doResolveViewMethod(component, declaredMethod);
        if (m == null) {
            return declaredMethod;
        }
        return m;
    }

    public void clearProxies() {
        cacheProxies.clear();
    }

    // does pretty much nothing
    // used only to get a layer between our EJB proxies and OWB proxies to let them manage the scope
    // /!\ don't extend AbstractOwbBean without checking equals()
    private static class InstanceBean<T> implements OwbBean<T>, PassivationCapable {
        private final CdiEjbBean<T> bean;
        private T owbProxy;

        public InstanceBean(final CdiEjbBean<T> tCdiEjbBean) {
            bean = tCdiEjbBean;
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
            if (owbProxy != null && SessionBeanType.STATEFUL.equals(bean.getEjbType())) { // we need to be able to remove OWB proxy to remove (statefuls for instance)
                bean.storeStatefulInstance(owbProxy, instance);
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
        public Producer<T> getProducer() {
            return new EjbProducer<T>(this, bean);
        }

        @Override
        public WebBeansType getWebBeansType() {
            return bean.getWebBeansType();
        }

        @Override
        public Class<T> getReturnType() {
            return bean.getReturnType();
        }

        @Override
        public void setSpecializedBean(final boolean specialized) {
            // no-op
        }

        @Override
        public boolean isSpecializedBean() {
            return bean.isSpecializedBean();
        }

        @Override
        public void setEnabled(final boolean enabled) {
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
        public WebBeansContext getWebBeansContext() {
            return bean.getWebBeansContext();
        }

        public void setOwbProxy(final T owbProxy) {
            this.owbProxy = owbProxy;
        }

        @Override
        public boolean equals(final Object o) {
            if (AbstractOwbBean.class.isInstance(o)) {
                return bean.equals(o);
            }
            return InstanceBean.class.isInstance(o) && bean.equals(InstanceBean.class.cast(o).bean);
        }

        @Override
        public int hashCode() {
            return bean.hashCode();
        }
    }

    private static class EjbProducer<T> implements Producer<T> {
        private final CdiEjbBean<T> ejb;
        private final InstanceBean<T> instance;

        public EjbProducer(final InstanceBean<T> tInstanceBean, final CdiEjbBean<T> bean) {
            instance = tInstanceBean;
            this.ejb = bean;
        }

        @Override
        public T produce(final CreationalContext<T> creationalContext) {
            return instance.create(creationalContext);
        }

        @Override
        public void dispose(final T instance) {
            ejb.destroyComponentInstance(instance);
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }
    }
}
