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

package org.apache.openejb;

import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.openejb.cdi.CdiEjbBean;
import org.apache.openejb.cdi.ConstructorInjectionBean;
import org.apache.openejb.cdi.CurrentCreationalContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.ivm.ContextHandler;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.EjbTransactionUtil;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.DynamicProxyImplFactory;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.component.CdiInterceptorBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.context.creational.DependentCreationalContext;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.intercept.DecoratorHandler;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.InjectionTargetImpl;
import org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.xbean.recipe.ConstructionException;

import jakarta.ejb.ApplicationException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.Handle;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.SessionBean;
import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import javax.naming.Context;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SynchronizationType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;

@SuppressWarnings("unchecked")
public class BeanContext extends DeploymentContext {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, BeanContext.class);

    public static final String USER_INTERCEPTOR_KEY = "org.apache.openejb.default.system.interceptors";
    public static final String USER_INTERCEPTOR_SEPARATOR = ",| |;";

    private static final Field DEPENDENTS_OBJECTS;

    static {
        try {
            DEPENDENTS_OBJECTS = CreationalContextImpl.class.getDeclaredField("dependentObjects");
            DEPENDENTS_OBJECTS.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Invalid OpenWebBeans version", e);
        }
    }

    private ConstructorInjectionBean<Object> constructorInjectionBean;
    private final boolean passivable;

    public boolean isDynamicallyImplemented() {
        return proxyClass != null;
    }

    public void mergeOWBAndOpenEJBInfo() {
        final CdiEjbBean cdiEjbBean = get(CdiEjbBean.class);
        if (cdiEjbBean == null) {
            return;
        }

        final InjectionTargetImpl<?> injectionTarget = InjectionTargetImpl.class.cast(get(CdiEjbBean.class).getInjectionTarget());
        final InterceptorResolutionService.BeanInterceptorInfo info = injectionTarget.getInterceptorInfo();
        if (info == null) {
            return;
        }

        final Collection<Interceptor<?>> postConstructInterceptors = Collection.class.cast(Reflections.get(injectionTarget, "postConstructInterceptors"));
        final Collection<Interceptor<?>> preDestroyInterceptors = Collection.class.cast(Reflections.get(injectionTarget, "preDestroyInterceptors"));
        if (postConstructInterceptors != null) {
            for (final Interceptor<?> pc : postConstructInterceptors) {
                if (isEjbInterceptor(pc)) {
                    continue;
                }

                final InterceptorData interceptorData = createInterceptorData(pc);
                instanceScopedInterceptors.add(interceptorData);
                cdiInterceptors.add(interceptorData);
            }
        }
        if (preDestroyInterceptors != null) {
            for (final Interceptor<?> pd : preDestroyInterceptors) {
                if (isEjbInterceptor(pd)) {
                    continue;
                }
                if (postConstructInterceptors.contains(pd)) {
                    continue;
                }
                final InterceptorData interceptorData = createInterceptorData(pd);
                instanceScopedInterceptors.add(interceptorData);
                cdiInterceptors.add(interceptorData);
            }
        }

        for (final Map.Entry<Method, InterceptorResolutionService.BusinessMethodInterceptorInfo> entry : info.getBusinessMethodsInfo().entrySet()) {
            final Interceptor<?>[] interceptors = entry.getValue().getCdiInterceptors();
            if (interceptors == null) {
                continue;
            }

            for (final Interceptor<?> i : interceptors) {
                // already at class level, since we merge "hooks" in InterceptorData no need to add it again
                if (postConstructInterceptors.contains(i) || preDestroyInterceptors.contains(i)) {
                    continue;
                }
                final InterceptorData data = createInterceptorData(i);
                addCdiMethodInterceptor(entry.getKey(), data);
            }
            entry.getValue().setEjbInterceptors(new ArrayList<>());
            entry.getValue().setCdiInterceptors(new ArrayList<>());
        }

        // handled by OpenEJB now so clean up all duplication from OWB
        if (info.getSelfInterceptorBean() != null) {
            try {
                final Field field = InterceptorResolutionService.BeanInterceptorInfo.class.getDeclaredField("selfInterceptorBean");
                field.setAccessible(true);
                field.set(info, null);
            } catch (final Exception e) {
                // no-op
            }
        }
        Map.class.cast(Reflections.get(injectionTarget, "methodInterceptors")).clear();
        clear(Collection.class.cast(postConstructInterceptors));
        clear(Collection.class.cast(preDestroyInterceptors));
        clear(Collection.class.cast(Reflections.get(injectionTarget, "postConstructMethods")));
        clear(Collection.class.cast(Reflections.get(injectionTarget, "preDestroyMethods")));
        clear(Collection.class.cast(Reflections.get(info, "ejbInterceptors")));
        clear(Collection.class.cast(Reflections.get(info, "cdiInterceptors")));

        // OWB doesn't compute AROUND_INVOKE so let's do it
        final Method timeout = getEjbTimeout();
        if (timeout != null) {
            final AnnotatedType annotatedType = cdiEjbBean.getAnnotatedType();
            final AnnotationManager annotationManager = getWebBeansContext().getAnnotationManager();
            final Collection<Annotation> annotations = new HashSet<>(annotationManager.getInterceptorAnnotations(annotatedType.getAnnotations()));
            final Set<AnnotatedMethod<?>> methods = annotatedType.getMethods();
            for (final AnnotatedMethod<?> m : methods) {
                if (timeout.equals(m.getJavaMember())) {
                    annotations.addAll(annotationManager.getInterceptorAnnotations(m.getAnnotations()));
                    break;
                }
            }
            if (!annotations.isEmpty()) {
                for (final Interceptor<?> timeoutInterceptor : getWebBeansContext().getBeanManagerImpl()
                        .resolveInterceptors(InterceptionType.AROUND_TIMEOUT, AnnotationUtil.asArray(annotations))) {
                    if (isEjbInterceptor(timeoutInterceptor)) {
                        continue;
                    }
                    final InterceptorData data = createInterceptorData(timeoutInterceptor);
                    addCdiMethodInterceptor(timeout, data);
                }
            }
        }
    }

    private boolean isEjbInterceptor(final Interceptor<?> pc) {
        final Set<Annotation> interceptorBindings = pc.getInterceptorBindings();
        return interceptorBindings == null || interceptorBindings.isEmpty();
    }

    private InterceptorData createInterceptorData(final Interceptor<?> i) {
        final InterceptorData data;
        if (CdiInterceptorBean.class.isInstance(i)) {
            final CdiInterceptorBean cdiInterceptorBean = CdiInterceptorBean.class.cast(i);

            data = new InterceptorData(cdiInterceptorBean);
        } else { // TODO: here we are not as good as in previous since we loose inheritance for instance
            data = InterceptorData.scan(i.getBeanClass());
        }
        return data;
    }

    private static void clear(final Collection<?> c) {
        if (c != null) { // yeah can be null with dynamically impl beans
            c.clear();
        }
    }

    public interface BusinessLocalHome extends EJBLocalHome {

        Object create();
    }

    public interface BusinessLocalBeanHome extends EJBLocalHome {

        Object create();
    }

    public interface BusinessRemoteHome extends EJBHome {

        Object create();
    }

    public interface Removable {

        void $$remove();
    }

    public interface ServiceEndpoint {

    }

    public interface Timeout {

    }

    /**
     * This ManagedBean is added to all EjbModules to effectively
     * create a sharable "comp" namespace which can be used in
     * for components with undefined namespace such as CDI
     */
    public static class Comp {

        public static String openejbCompName(final String module) {
            return module + "." + Comp.class.getSimpleName();
        }
    }

    private final ModuleContext moduleContext;
    private final Context jndiContext;
    private Object containerData;

    private boolean destroyed;
    private final Class beanClass;
    private final List<Class> businessLocals = new ArrayList<>();
    private final List<Class> businessRemotes = new ArrayList<>();
    private Class serviceEndpointInterface;

    private Method ejbTimeout;
    private EjbTimerService ejbTimerService;

    private boolean isBeanManagedTransaction;
    private boolean isBeanManagedConcurrency;
    private Container container;

    private String ejbName;
    private String runAs;
    private String runAsUser;

    private final BeanType componentType;

    private boolean hidden;

    //private final Map<Method, TransactionType> methodTransactionType = new HashMap<Method, TransactionType>();
    private final Map<Method, Method> methodMap = new HashMap<>();
    private final Map<Method, MethodContext> methodContextMap = new HashMap<>();
    private final Map<String, ViewContext> viewContextMap = new HashMap<>();

    private TransactionPolicyFactory transactionPolicyFactory;

    private final List<InterceptorData> callbackInterceptors = new ArrayList<>();
    private final List<InterceptorData> beanCallbackInterceptors = new ArrayList<>();
    private final Set<InterceptorData> instanceScopedInterceptors = new HashSet<>();
    private final List<InterceptorInstance> systemInterceptors = new ArrayList<>();
    private final List<InterceptorInstance> userInterceptors = new ArrayList<>();
    private final List<Injection> injections = new ArrayList<>();
    private final Map<Class, InterfaceType> interfaces = new HashMap<>();
    private final Map<Class, ExceptionType> exceptions = new ConcurrentHashMap<>();

    private final boolean localbean;
    private Duration accessTimeout;

    private final Set<Class<?>> asynchronousClasses = new HashSet<>();
    private final Set<String> asynchronousMethodSignatures = new HashSet<>();
    private Class<?> proxyClass;

    private Mdb mdb;
    private Singleton singleton;
    private Stateful stateful;
    private Cmp cmp;
    private LegacyView legacyView;

    private final Map<String, String> securityRoleReferences = new HashMap<>();

    /**
     * TODO: Move to MethodContext
     */
    private final Map<Method, Boolean> removeExceptionPolicy = new HashMap<>();

    public Class getInterface(final InterfaceType interfaceType) {
        switch (interfaceType) {
            case EJB_HOME:
                return getHomeInterface();
            case EJB_OBJECT:
                return getRemoteInterface();
            case EJB_LOCAL_HOME:
                return getLocalHomeInterface();
            case EJB_LOCAL:
                return getLocalInterface();
            case BUSINESS_LOCAL:
                return getBusinessLocalInterface();
            case BUSINESS_REMOTE:
                return getBusinessRemoteInterface();
            case TIMEOUT:
                return BeanContext.Timeout.class;
            case BUSINESS_REMOTE_HOME:
                return BeanContext.BusinessRemoteHome.class;
            case BUSINESS_LOCAL_HOME:
                return BeanContext.BusinessLocalHome.class;
            case SERVICE_ENDPOINT:
                return getServiceEndpointInterface();
            case LOCALBEAN:
                return getBeanClass();
            case BUSINESS_LOCALBEAN_HOME:
                return BeanContext.BusinessLocalBeanHome.class;
            default:
                throw new IllegalStateException("Unexpected enum constant: " + interfaceType);
        }
    }

    public List<Class> getInterfaces(final InterfaceType interfaceType) {
        switch (interfaceType) {
            case BUSINESS_REMOTE:
                return getBusinessRemoteInterfaces();
            case BUSINESS_LOCAL:
                return getBusinessLocalInterfaces();
            default:
                final List<Class> interfaces = new ArrayList<>();
                interfaces.add(getInterface(interfaceType));
                return interfaces;
        }
    }

    public InterfaceType getInterfaceType(final Class clazz) {
        final InterfaceType type = interfaces.get(clazz);
        if (type != null) {
            return type;
        }

        if (EJBLocalHome.class.isAssignableFrom(clazz)) {
            return InterfaceType.EJB_LOCAL_HOME;
        }
        if (EJBLocalObject.class.isAssignableFrom(clazz)) {
            return InterfaceType.EJB_LOCAL;
        }
        if (EJBHome.class.isAssignableFrom(clazz)) {
            return InterfaceType.EJB_HOME;
        }
        if (EJBObject.class.isAssignableFrom(clazz)) {
            return InterfaceType.EJB_OBJECT;
        }

        for (final Entry<Class, InterfaceType> entry : interfaces.entrySet()) { // for @Remote case where the loaded interface can be different from the stored one
            if (entry.getKey().getName().equals(clazz.getName())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * load default interceptors configured in properties.
     */
    private BeanContext(final String id, final Context jndiContext, final ModuleContext moduleContext, final BeanType componentType,
                        final boolean localBean, final Class beanClass, final boolean passivable) {
        super(id, moduleContext.getOptions());

        if (beanClass == null) {
            throw new NullPointerException("beanClass input parameter is null");
        }

        this.moduleContext = moduleContext;
        this.jndiContext = new ContextHandler(jndiContext);
        this.localbean = localBean;
        this.componentType = componentType;
        this.beanClass = beanClass;
        this.passivable = passivable;

        final String interceptors = SystemInstance.get().getProperties().getProperty(USER_INTERCEPTOR_KEY);
        if (interceptors != null) {
            final String[] interceptorArray = interceptors.split(USER_INTERCEPTOR_SEPARATOR);
            final ClassLoader classLoader = moduleContext.getClassLoader();
            for (final String interceptor : interceptorArray) {
                if (interceptor != null && !interceptor.isEmpty()) {
                    final Object interceptorObject;
                    try {
                        final Class<?> clazz = classLoader.loadClass(interceptor);
                        interceptorObject = clazz.newInstance();
                    } catch (final Exception e) {
                        logger.warning("interceptor " + interceptor + " not found, are you sure the container can load it?");
                        continue;
                    }
                    addUserInterceptor(interceptorObject);
                }
            }
        }
    }

    public BeanContext(final String id, final Context jndiContext, final ModuleContext moduleContext,
                       final Class beanClass, final Class homeInterface,
                       final Class remoteInterface,
                       final Class localHomeInterface,
                       final Class localInterface,
                       final Class proxy,
                       final Class serviceEndpointInterface, final List<Class> businessLocals, final List<Class> businessRemotes, final Class pkClass,
                       final BeanType componentType,
                       final boolean localBean,
                       final boolean passivable) throws SystemException {
        this(id, jndiContext, moduleContext, componentType, localBean, beanClass, passivable);

        this.proxyClass = proxy;

        if (homeInterface != null) {
            this.getLegacyView().homeInterface = homeInterface;
        }
        if (localInterface != null) {
            this.getLegacyView().localInterface = localInterface;
        }
        if (localHomeInterface != null) {
            this.getLegacyView().localHomeInterface = localHomeInterface;
        }
        if (remoteInterface != null) {
            this.getLegacyView().remoteInterface = remoteInterface;
        }

        if (businessLocals != null) {
            this.businessLocals.addAll(businessLocals);
        }
        if (businessRemotes != null) {
            this.businessRemotes.addAll(businessRemotes);
        }

        if (pkClass != null) {
            getCmp().pkClass = pkClass;
        }

        this.serviceEndpointInterface = serviceEndpointInterface;

        //        if (businessLocal != null && localHomeInterface == null){
        //            this.localHomeInterface = BusinessLocalHome.class;
        //        }
        //
        //        if (businessRemote != null && homeInterface == null){
        //            this.homeInterface = BusinessRemoteHome.class;
        //        }

        // createMethodMap();

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout", Timer.class);
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        addInterface(getServiceEndpointInterface(), InterfaceType.SERVICE_ENDPOINT);

        addInterface(EJBHome.class, InterfaceType.EJB_HOME);
        addInterface(EJBObject.class, InterfaceType.EJB_OBJECT);

        addInterface(EJBLocalHome.class, InterfaceType.EJB_LOCAL_HOME);
        addInterface(EJBLocalObject.class, InterfaceType.EJB_LOCAL);

        addInterface(getHomeInterface(), InterfaceType.EJB_HOME);
        addInterface(getRemoteInterface(), InterfaceType.EJB_OBJECT);

        addInterface(getLocalHomeInterface(), InterfaceType.EJB_LOCAL_HOME);
        addInterface(getLocalInterface(), InterfaceType.EJB_LOCAL);

        addInterface(BeanContext.BusinessRemoteHome.class, InterfaceType.BUSINESS_REMOTE_HOME);
        for (final Class businessRemote : this.businessRemotes) {
            addInterface(businessRemote, InterfaceType.BUSINESS_REMOTE);
        }

        addInterface(BeanContext.BusinessLocalHome.class, InterfaceType.BUSINESS_LOCAL_HOME);
        for (final Class businessLocal : this.businessLocals) {
            addInterface(businessLocal, InterfaceType.BUSINESS_LOCAL);
        }
        if (localBean) {
            addInterface(beanClass, InterfaceType.LOCALBEAN);
        }

        this.initDefaultLock();
    }

    private LegacyView getLegacyView() {
        if (legacyView == null) {
            legacyView = new LegacyView();
        }
        return legacyView;
    }

    private Mdb getMdb() {
        if (mdb == null) {
            mdb = new Mdb();
        }
        return mdb;
    }

    private Singleton getSingleton() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }

    private Stateful getStateful() {
        if (stateful == null) {
            stateful = new Stateful();
        }
        return stateful;
    }

    private Cmp getCmp() {
        if (cmp == null) {
            cmp = new Cmp();
        }
        return cmp;
    }

    /**
     * DMB: This is a not so reliable way to determine the proxy type
     * The proxy type really should come with the call in the invoke.
     *
     * @param interfce Class
     * @param type     InterfaceType
     */
    private void addInterface(final Class interfce, final InterfaceType type) {
        if (interfce == null) {
            return;
        }
        interfaces.put(interfce, type);

        for (final Class clazz : interfce.getInterfaces()) {
            addInterface(clazz, type);
        }
    }

    public void addApplicationException(final Class exception, final boolean rollback, final boolean inherited) {
        if (inherited) {
            if (rollback) {
                exceptions.put(exception, ExceptionType.APPLICATION_ROLLBACK);
            } else {
                exceptions.put(exception, ExceptionType.APPLICATION);
            }
        } else {
            if (rollback) {
                exceptions.put(exception, ExceptionType.APPLICATION_ROLLBACK_NOT_INHERITED);
            } else {
                exceptions.put(exception, ExceptionType.APPLICATION_NOT_INHERITED);
            }
        }
    }

    public ExceptionType getExceptionType(final Throwable e) {
        // Errors are always system exceptions
        if (!(e instanceof Exception)) {
            return ExceptionType.SYSTEM;
        }

        // check the registered app exceptions
        Class<?> exceptionClass = e.getClass();
        boolean inherited = false;
        while (exceptionClass != Object.class) {
            final ExceptionType type = exceptions.get(exceptionClass);
            if (type == ExceptionType.APPLICATION || type == ExceptionType.APPLICATION_ROLLBACK) {
                return type;
            }
            if (type != null) {
                if (inherited) {
                    return ExceptionType.SYSTEM;
                }
                if (type == ExceptionType.APPLICATION_NOT_INHERITED) {
                    return ExceptionType.APPLICATION;
                }
                return ExceptionType.APPLICATION_ROLLBACK;
            }
            exceptionClass = exceptionClass.getSuperclass();
            inherited = true;
        }

        // Unregistered - runtime exceptions are system exception and the rest are application exceptions
        final Class<? extends Throwable> eClass = e.getClass();
        final ApplicationException applicationException = eClass.getAnnotation(ApplicationException.class);
        if (applicationException != null) {
            addApplicationException(eClass, applicationException.rollback(), applicationException.inherited());
            return getExceptionType(e);
        }

        if (e instanceof RuntimeException) {
            return ExceptionType.SYSTEM;
        }
        return ExceptionType.APPLICATION;
    }

    public BeanContext(final String id, final Context jndiContext, final ModuleContext moduleContext, final Class beanClass, final Class mdbInterface, final Map<String, String> activationProperties) throws SystemException {
        this(id, jndiContext, moduleContext, BeanType.MESSAGE_DRIVEN, false, beanClass, false);

        this.getMdb().mdbInterface = mdbInterface;
        this.getMdb().activationProperties.putAll(activationProperties);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout", Timer.class);
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        this.initDefaultLock();

        this.createMethodMap();
    }

    private void initDefaultLock() {

        if (!BeanType.SINGLETON.equals(this.componentType)) {
            return;
        }

        final ArrayList<Class> classes = new ArrayList<>();

        classes.addAll(businessRemotes);

        classes.addAll(businessLocals);

        classes.add(this.beanClass);

        for (final Class c : classes) {
            Lock lock = null;
            try {

                lock = (Lock) c.getAnnotation(Lock.class);
                this.getSingleton().lockType = lock.value();

                if (logger.isDebugEnabled()) {
                    logger.debug("Declared Lock for " + c.getName() + " is " + this.getSingleton().lockType);
                }

            } catch (final NullPointerException e) {
                //Ignore
            } catch (final Throwable e) {
                logger.warning("Failed to determine from: " + lock);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Default Lock for " + this.beanClass.getName() + " is " + this.getSingleton().lockType);
        }
    }

    public Object getContainerData() {
        return containerData;
    }

    public void setContainerData(final Object containerData) {
        this.containerData = containerData;
    }

    public ClassLoader getClassLoader() {
        return moduleContext.getClassLoader();
    }

    public Context getJndiContext() {
        return jndiContext;
    }

    public ModuleContext getModuleContext() {
        return moduleContext;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }

    public List<Injection> getInjections() {
        return injections;
    }

    public Index<EntityManagerFactory, EntityManagerConfiguration> getExtendedEntityManagerFactories() {
        return getStateful().extendedEntityManagerFactories;
    }

    public void setExtendedEntityManagerFactories(final Index<EntityManagerFactory, EntityManagerConfiguration> extendedEntityManagerFactories) {
        this.getStateful().extendedEntityManagerFactories = extendedEntityManagerFactories;
    }

    public void setContainer(final Container container) {
        this.container = container;
    }

    public BeanType getComponentType() {
        return componentType;
    }

    public LockType getConcurrencyAttribute(final Method beanMethod) {
        return getMethodContext(beanMethod).getLockType();
    }

    public LockType getLockType() {
        return this.getSingleton().lockType;
    }

    public TransactionType getTransactionType(final Method method) {
        return getTransactionType(method, null);
    }

    public TransactionType getTransactionType(final Method method, final InterfaceType interfaceType) {

        MethodContext methodContext = null;

        if (interfaceType != null) {
            methodContext = getViewMethodContext(method, interfaceType.getSpecName());
        }

        if (methodContext == null) {
            methodContext = methodContextMap.get(method);
        }

        if (methodContext == null) {
            final Method beanMethod = getMatchingBeanMethod(method);
            methodContext = getMethodContext(beanMethod);
        }

        return methodContext.getTransactionType();

        //
        //        // Check the cache
        //        TransactionType type = methodTransactionType.get(method);
        //        if (type != null) {
        //            return type;
        //        }
        //
        //        // Bean managed EJBs always get the BeanManaged policy
        //        if (isBeanManagedTransaction) {
        //            return TransactionType.BeanManaged;
        //        }
        //
        //        // Check the matching bean method for the supplied method
        //        Method beanMethod = getMatchingBeanMethod(method);
        //        if (beanMethod != null){
        //            type = methodTransactionType.get(beanMethod);
        //            if (type != null) {
        //                return type;
        //            }
        //        }
        //
        //        // All transaction attributes should have been set during deployment, so log a message
        //        Logger log = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
        //        log.debug("The following method doesn't have a transaction policy assigned: " + method);
        //
        //        // default transaction policy is required
        //        type = getTransactionType();
        //
        //        // cache this default to avoid extra log messages
        //        methodTransactionType.put(method, type);
        //        return type;
    }

    public TransactionType getTransactionType() {
        return isBeanManagedTransaction ? TransactionType.BeanManaged : TransactionType.Required;
    }

    public TransactionPolicyFactory getTransactionPolicyFactory() {
        return transactionPolicyFactory;
    }

    public void setTransactionPolicyFactory(final TransactionPolicyFactory transactionPolicyFactory) {
        this.transactionPolicyFactory = transactionPolicyFactory;
    }

    public Container getContainer() {
        return container;
    }

    public Object getDeploymentID() {
        return getId();
    }

    public boolean isBeanManagedTransaction() {
        return isBeanManagedTransaction;
    }

    public boolean isBeanManagedConcurrency() {
        return isBeanManagedConcurrency;
    }

    public Class getHomeInterface() {
        return legacyView == null ? null : getLegacyView().homeInterface;
    }

    public Class getRemoteInterface() {
        return legacyView == null ? null : getLegacyView().remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return legacyView == null ? null : getLegacyView().localHomeInterface;
    }

    public Class getLocalInterface() {
        return legacyView == null ? null : getLegacyView().localInterface;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public Class getBusinessLocalInterface() {
        return businessLocals.size() > 0 ? businessLocals.get(0) : null;
    }

    public Class getBusinessRemoteInterface() {
        return businessRemotes.size() > 0 ? businessRemotes.get(0) : null;
    }

    public List<Class> getBusinessLocalInterfaces() {
        return businessLocals;
    }

    public List<Class> getBusinessRemoteInterfaces() {
        return businessRemotes;
    }

    public Class getMdbInterface() {
        return mdb == null ? null : getMdb().mdbInterface;
    }

    public Class getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public Map<String, String> getActivationProperties() {
        return getMdb().activationProperties;
    }

    public Class getPrimaryKeyClass() {
        return cmp == null ? null : cmp.pkClass;
    }

    public EJBHome getEJBHome() {
        if (getHomeInterface() == null) {
            throw new IllegalStateException("This component has no home interface: " + getDeploymentID());
        }
        if (getLegacyView().ejbHomeRef == null) {
            getLegacyView().ejbHomeRef = (EJBHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.EJB_HOME);
        }
        return getLegacyView().ejbHomeRef;
    }

    public EJBLocalHome getEJBLocalHome() {
        if (getLocalHomeInterface() == null) {
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (getLegacyView().ejbLocalHomeRef == null) {
            getLegacyView().ejbLocalHomeRef = (EJBLocalHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.EJB_LOCAL_HOME);
        }
        return getLegacyView().ejbLocalHomeRef;
    }

    //unused
    public BusinessLocalHome getBusinessLocalHome() {
        return getBusinessLocalHome(getBusinessLocalInterfaces(), null);
    }

    public BusinessLocalBeanHome getBusinessLocalBeanHome() {
        final List<Class> interfaces = new ArrayList<>();
        interfaces.add(this.beanClass);
        return (BusinessLocalBeanHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCALBEAN_HOME, interfaces, this.beanClass);
    }

    public BusinessLocalHome getBusinessLocalHome(final Class mainInterface) {
        final List<Class> localInterfaces = this.getBusinessLocalInterfaces();

        final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(this.getBeanClass(), mainInterface, localInterfaces);
        return this.getBusinessLocalHome(interfaces, mainInterface);
    }

    public BusinessLocalHome getBusinessLocalHome(final List<Class> interfaces, final Class mainInterface) {
        if (getBusinessLocalInterfaces().size() == 0) {
            throw new IllegalStateException("This component has no business local interfaces: " + getDeploymentID());
        }
        if (interfaces.size() == 0) {
            throw new IllegalArgumentException("No interface classes were specified");
        }
        for (final Class clazz : interfaces) {
            if (!getBusinessLocalInterfaces().contains(clazz)
                    && !getBusinessRemoteInterfaces().contains(clazz) /* for CDI mainly */) {
                throw new IllegalArgumentException("Not a business interface of this bean:" + clazz.getName());
            }
        }

        return (BusinessLocalHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCAL_HOME, interfaces, mainInterface);
    }

    //unused
    public BusinessRemoteHome getBusinessRemoteHome() {
        return getBusinessRemoteHome(getBusinessRemoteInterfaces(), null);
    }

    public BusinessRemoteHome getBusinessRemoteHome(final Class mainInterface) {
        final List<Class> remoteInterfaces = this.getBusinessRemoteInterfaces();

        final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(this.getBeanClass(), mainInterface, remoteInterfaces);
        return this.getBusinessRemoteHome(interfaces, mainInterface);
    }

    public BusinessRemoteHome getBusinessRemoteHome(final List<Class> interfaces, final Class mainInterface) {
        if (getBusinessRemoteInterfaces().size() == 0) {
            throw new IllegalStateException("This component has no business remote interfaces: " + getDeploymentID());
        }
        if (interfaces.size() == 0) {
            throw new IllegalArgumentException("No interface classes were specified");
        }
        for (final Class clazz : interfaces) {
            if (!getBusinessRemoteInterfaces().contains(clazz)) {
                throw new IllegalArgumentException("Not a business interface of this bean:" + clazz.getName());
            }
        }

        return (BusinessRemoteHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_REMOTE_HOME, interfaces, mainInterface);
    }

    public String getDestinationId() {
        return getMdb().destinationId;
    }

    public void setDestinationId(final String destinationId) {
        this.getMdb().destinationId = destinationId;
    }

    public void setBeanManagedTransaction(final boolean value) {
        isBeanManagedTransaction = value;
    }

    public void setBeanManagedConcurrency(final boolean beanManagedConcurrency) {
        isBeanManagedConcurrency = beanManagedConcurrency;
    }

    public Context getJndiEnc() {
        return jndiContext;
    }

    public boolean isReentrant() {
        return getCmp().isReentrant;
    }

    public void setIsReentrant(final boolean reentrant) {
        getCmp().isReentrant = reentrant;
    }

    public Method getMatchingBeanMethod(final Method interfaceMethod) {
        final Method method = methodMap.get(interfaceMethod);
        return method == null ? interfaceMethod : method;
    }

    public MethodContext getMethodContext(final Method method) {
        MethodContext methodContext = methodContextMap.get(method);
        if (methodContext == null) {
            methodContext = new MethodContext(this, method);
            methodContextMap.put(method, methodContext);
        }
        return methodContext;
    }

    // TODO The MethodContext object has Method as a variable, so we could change this
    // to simply return methodContextMap.values() which would be cleaner
    public Iterator<Entry<Method, MethodContext>> iteratorMethodContext() {
        return methodContextMap.entrySet().iterator();
    }

    public void setMethodConcurrencyAttribute(final Method method, final LockType concurrencyAttribute) {
        getMethodContext(method).setLockType(concurrencyAttribute);
    }

    /**
     * TODO: Move to MethodContext
     */
    public void setMethodTransactionAttribute(final Method method, final TransactionType transactionType) throws OpenEJBException {
        setMethodTransactionAttribute(method, transactionType, null);
    }

    /**
     * TODO: Move to MethodContext
     */
    public void setMethodTransactionAttribute(final Method method, final TransactionType transactionType, final String view) throws OpenEJBException {

        //        method = getMatchingBeanMethod(method);

        if (view == null) {
            getMethodContext(method).setTransactionType(transactionType);
        } else {
            initViewMethodContext(method, view).setTransactionType(transactionType);
        }

        //        // Only the NOT_SUPPORTED and REQUIRED transaction attributes may be used for message-driven
        //        // bean message listener methods. The use of the other transaction attributes is not meaningful
        //        // for message-driven bean message listener methods because there is no pre-existing client transaction
        //        // context(REQUIRES_NEW, SUPPORTS) and no client to handle exceptions (MANDATORY, NEVER).
        //        if (componentType.isMessageDriven() && !isBeanManagedTransaction) {
        //            if (transactionType != TransactionType.NotSupported && transactionType != TransactionType.Required) {
        //
        //                if ((method.equals(this.ejbTimeout) || methodContextMap.get(method).isAsynchronous()) && transactionType == TransactionType.RequiresNew) {
        //                    // do nothing. This is allowed as the timer callback method for a message driven bean
        //                    // can also have a transaction policy of RequiresNew Sec 5.4.12 of Ejb 3.0 Core Spec
        //                } else {
        //                    throw new OpenEJBException("The transaction attribute " + transactionType + " is not supported for the method "
        //                                               + method.getName() + " of the Message Driven Bean " + beanClass.getName());
        //                }
        //            }
        //        }
        //        methodTransactionType.put(method, transactionType);
    }

    public List<Method> getRemoveMethods() {
        return getStateful().removeMethods;
    }

    /**
     * TODO: Move to MethodContext
     */
    public void setRetainIfExeption(final Method removeMethod, final boolean retain) {
        if (getRemoveMethods().contains(removeMethod)) {
            removeExceptionPolicy.put(removeMethod, retain);
        }
    }

    /**
     * TODO: Move to MethodContext
     */
    public boolean retainIfExeption(final Method removeMethod) {
        final Boolean retain = removeExceptionPolicy.get(removeMethod);
        return retain != null && retain;
    }

    /**
     * When an instance of an EJB is instantiated, everything in this list
     * is also instatiated and tied to the bean instance.  Per spec, interceptors
     * are supposed to have the same lifecycle as the bean they wrap.
     *
     * OpenEJB has the concept of interceptors which do not share the same lifecycle
     * as the bean instance -- they may be instantiated elsewhere and simply applied
     * to the bean.  The impact is that these interceptors must be multithreaded.
     * It also means we do not add these interceptors to this list and expose them
     * via different means.
     *
     * @return standard interceptors sharing the bean lifecycle
     */
    public Set<InterceptorData> getInstanceScopedInterceptors() {
        return instanceScopedInterceptors;
    }

    public void addSystemInterceptor(final Object interceptor) {
        systemInterceptors.add(new InterceptorInstance(interceptor));
    }

    public void addFirstSystemInterceptor(final Object interceptor) {
        systemInterceptors.add(0, new InterceptorInstance(interceptor));
    }

    public void addUserInterceptor(final Object interceptor) {
        userInterceptors.add(new InterceptorInstance(interceptor));
    }

    public List<InterceptorInstance> getUserAndSystemInterceptors() {
        final List<InterceptorInstance> interceptors = new ArrayList<>(systemInterceptors);
        interceptors.addAll(userInterceptors);
        return interceptors;
    }

    private final Set<InterceptorData> cdiInterceptors = new LinkedHashSet<>();

    public List<InterceptorData> getCallbackInterceptors() {
        final List<InterceptorData> datas = getInterceptorData();
        datas.addAll(callbackInterceptors);
        datas.addAll(cdiInterceptors);
        datas.addAll(beanCallbackInterceptors);
        return datas;
    }

    public void setCallbackInterceptors(final List<InterceptorData> callbackInterceptors) {
        //TODO shouldn't we remove the old callbackInterceptors from instanceScopedInterceptors before adding the new ones?
        this.beanCallbackInterceptors.clear();
        this.callbackInterceptors.clear();
        for (final InterceptorData data : callbackInterceptors) {
            if (data.getInterceptorClass().isAssignableFrom(getManagedClass())) {
                this.beanCallbackInterceptors.add(data);
            } else {
                this.callbackInterceptors.add(data);
            }
        }
        this.instanceScopedInterceptors.addAll(callbackInterceptors);
    }

    public List<InterceptorData> getCdiInterceptors() {
        return new ArrayList<>(cdiInterceptors);
    }

    public void setCdiInterceptors(final List<InterceptorData> cdiInterceptors) {
        this.cdiInterceptors.clear();
        this.cdiInterceptors.addAll(cdiInterceptors);
        this.instanceScopedInterceptors.addAll(cdiInterceptors);
    }

    public List<InterceptorData> getMethodInterceptors(final Method method) {
        return getMethodContext(method).getInterceptors();
    }

    public List<InterceptorData> getInterceptorData() {
        final List<InterceptorData> datas = new ArrayList<>(getUserAndSystemInterceptors().size());
        for (final InterceptorInstance instance : getUserAndSystemInterceptors()) {
            datas.add(instance.getData());
        }
        return datas;
    }

    public void addCdiMethodInterceptor(final Method method, final InterceptorData interceptor) {
        getMethodContext(method).addCdiInterceptor(interceptor);
        instanceScopedInterceptors.add(interceptor);
    }

    public void setMethodInterceptors(final Method method, final List<InterceptorData> interceptors) {
        getMethodContext(method).setInterceptors(interceptors);
        this.instanceScopedInterceptors.addAll(interceptors);
    }

    public void createMethodMap() throws SystemException {
        if (getRemoteInterface() != null) {
            mapObjectInterface(getLegacyView().remoteInterface);
            mapHomeInterface(getLegacyView().homeInterface);
        }

        if (getLocalInterface() != null) {
            mapObjectInterface(getLegacyView().localInterface);
        }
        if (getLocalHomeInterface() != null) {
            mapHomeInterface(getLegacyView().localHomeInterface);
        }

        if (serviceEndpointInterface != null) {
            mapObjectInterface(serviceEndpointInterface);
        }

        for (final Class businessLocal : businessLocals) {
            mapObjectInterface(businessLocal);
        }

        for (final Class businessRemote : businessRemotes) {
            mapObjectInterface(businessRemote);
        }

        if (componentType == BeanType.MESSAGE_DRIVEN && MessageDrivenBean.class.isAssignableFrom(beanClass)) {
            try {
                getLegacyView().createMethod = beanClass.getMethod("ejbCreate");
            } catch (final NoSuchMethodException e) {
                // if there isn't an ejbCreate method that is fine
            }
        }

        try {
            // map the remove methods
            if (componentType == BeanType.STATEFUL || componentType == BeanType.MANAGED) {

                Method beanMethod = null;
                if (SessionBean.class.isAssignableFrom(beanClass)) {
                    beanMethod = SessionBean.class.getDeclaredMethod("ejbRemove");
                } else {
                    for (final Method method : getRemoveMethods()) {
                        if (method.getParameterTypes().length == 0) {
                            beanMethod = method;
                            break;
                        }
                    }
                    if (beanMethod == null && (getHomeInterface() != null || getLocalHomeInterface() != null)) {
                        throw new IllegalStateException("Bean class has no @Remove methods to match EJBObject.remove() or EJBLocalObject.remove().  A no-arg remove method must be added: beanClass=" + beanClass.getName());
                    }
                }

                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", Handle.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", Object.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBLocalObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
            } else if (componentType == BeanType.BMP_ENTITY || componentType == BeanType.CMP_ENTITY) {
                final Method beanMethod = EntityBean.class.getDeclaredMethod("ejbRemove");
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", Handle.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", Object.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBLocalObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
            }
        } catch (final NoSuchMethodException nsme) {
            throw new SystemException(nsme);
        }

        if (mdb != null && mdb.mdbInterface != null) {
            mapObjectInterface(getMdb().mdbInterface);
        }
    }

    private void mapHomeInterface(final Class intrface) {
        final Method[] homeMethods = intrface.getMethods();
        for (final Method method : homeMethods) {
            final Class owner = method.getDeclaringClass();
            if (owner == EJBHome.class || owner == EJBLocalHome.class) {
                continue;
            }

            try {
                Method beanMethod = null;
                if (method.getName().startsWith("create")) {
                    final StringBuilder ejbCreateName = new StringBuilder(method.getName());
                    ejbCreateName.replace(0, 1, "ejbC");
                    beanMethod = beanClass.getMethod(ejbCreateName.toString(), method.getParameterTypes());
                    getLegacyView().createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching
                    parameters. This code maps that relationship.
                    */
                    if (this.componentType == BeanType.BMP_ENTITY || this.componentType == BeanType.CMP_ENTITY) {
                        ejbCreateName.insert(3, "Post");
                        Class clazz = beanClass;
                        if (getCmp().cmpImplClass != null) {
                            clazz = getCmp().cmpImplClass;
                        }
                        final Method postCreateMethod = clazz.getMethod(ejbCreateName.toString(), method.getParameterTypes());
                        getCmp().postCreateMethodMap.put(getLegacyView().createMethod, postCreateMethod);
                    }
                    /*
                     * Stateless session beans only have one create method. The getCreateMethod is
                     * used by instance manager of the core.stateless.StatelessContainer as a convenience
                     * method for obtaining the ejbCreate method.
                    */
                } else if (method.getName().startsWith("find")) {
                    if (this.componentType == BeanType.BMP_ENTITY) {

                        final String beanMethodName = "ejbF" + method.getName().substring(1);
                        beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                    }
                } else {
                    final String beanMethodName = "ejbHome" + method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1);
                    beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                }
                if (beanMethod != null) {
                    mapMethods(method, beanMethod);
                }
            } catch (final NoSuchMethodException nsme) {
                //                throw new OpenEJBRuntimeException("Invalid method [" + method + "] Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    public void mapMethods(final Method interfaceMethod, final Method beanMethod) {
        methodMap.put(interfaceMethod, beanMethod);
    }

    private void mapObjectInterface(final Class intrface) {
        if (intrface == BusinessLocalHome.class || intrface == BusinessRemoteHome.class || intrface == ServiceEndpoint.class) {
            return;
        }

        final Method[] interfaceMethods = intrface.getMethods();
        for (final Method method : interfaceMethods) {
            final Class declaringClass = method.getDeclaringClass();
            if (declaringClass == EJBObject.class || declaringClass == EJBLocalObject.class) {
                continue;
            }
            try {
                final Method beanMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());
                mapMethods(method, beanMethod);
            } catch (final NoSuchMethodException nsme) {
                throw new OpenEJBRuntimeException("Invalid method [" + method + "]. Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    public List<Class> getObjectInterface(final Class homeInterface) {
        if (BusinessLocalHome.class.isAssignableFrom(homeInterface)) {
            return getBusinessLocalInterfaces();
        } else if (BusinessRemoteHome.class.isAssignableFrom(homeInterface)) {
            return getBusinessRemoteInterfaces();
        } else if (EJBLocalHome.class.isAssignableFrom(homeInterface)) {
            final List<Class> classes = new ArrayList<>();
            classes.add(getLocalInterface());
            return classes;
        } else if (EJBHome.class.isAssignableFrom(homeInterface)) {
            final List<Class> classes = new ArrayList<>();
            classes.add(getRemoteInterface());
            return classes;
        } else {
            throw new IllegalArgumentException("Cannot determine object interface for " + homeInterface);
        }
    }

    public Method getCreateMethod() {
        return getLegacyView().createMethod;
    }

    public Method getMatchingPostCreateMethod(final Method createMethod) {
        return this.getCmp().postCreateMethodMap.get(createMethod);
    }

    public boolean isAsynchronous(final Method method) {
        final Method matchingBeanMethod = getMatchingBeanMethod(method);
        final Class<?> returnType = matchingBeanMethod.getReturnType();
        if (returnType != void.class && returnType != Future.class) {
            return false;
        }
        if (asynchronousClasses.contains(matchingBeanMethod.getDeclaringClass())) {
            return true;
        }
        final MethodContext methodContext = methodContextMap.get(matchingBeanMethod);
        return methodContext != null && methodContext.isAsynchronous();
    }

    public boolean isCmp2() {
        return getCmp().cmp2;
    }

    public void setCmp2(final boolean cmp2) {
        this.getCmp().cmp2 = cmp2;
    }

    public String getPrimaryKeyField() {
        return getCmp().primaryKeyField;
    }

    public void setPrimaryKeyField(final String primaryKeyField) {
        this.getCmp().primaryKeyField = primaryKeyField;
    }

    public KeyGenerator getKeyGenerator() {
        return getCmp().keyGenerator;
    }

    public void setKeyGenerator(final KeyGenerator keyGenerator) {
        this.getCmp().keyGenerator = keyGenerator;
    }

    public void setRemoteQueryResults(final String methodSignature) {
        getCmp().remoteQueryResults.add(methodSignature);
    }

    public boolean isRemoteQueryResults(final String methodSignature) {
        return getCmp().remoteQueryResults.contains(methodSignature);
    }

    public Class getCmpImplClass() {
        return getCmp().cmpImplClass;
    }

    public void setCmpImplClass(final Class cmpImplClass) {
        this.getCmp().cmpImplClass = cmpImplClass;
    }

    public String getAbstractSchemaName() {
        return getCmp().abstractSchemaName;
    }

    public void setAbstractSchemaName(final String abstractSchemaName) {
        this.getCmp().abstractSchemaName = abstractSchemaName;
    }

    public Method getEjbTimeout() {
        return ejbTimeout;
    }

    public void setEjbTimeout(final Method ejbTimeout) {
        this.ejbTimeout = ejbTimeout;
    }

    public EjbTimerService getEjbTimerService() {
        return ejbTimerService;
    }

    public void setEjbTimerService(final EjbTimerService ejbTimerService) {
        this.ejbTimerService = ejbTimerService;
    }

    public String getEjbName() {
        return ejbName;
    }

    public String getModuleID() {
        return getModuleContext().getModuleURI().toString();
    }

    public String getModuleName() {
        return getModuleContext().getId();
    }

    public String getRunAs() {
        return runAs;
    }

    public String getRunAsUser() {
        return runAsUser;
    }

    public void setEjbName(final String ejbName) {
        this.ejbName = ejbName;
    }

    public void setRunAs(final String runAs) {
        this.runAs = runAs;
        if (runAsUser == null) { // default user
            runAsUser = runAs;
        }
    }

    public void setRunAsUser(final String runAsUser) { // principal
        if (runAsUser != null) {
            this.runAsUser = runAsUser;
        }
    }

    public String toString() {
        return "BeanContext(id=" + getDeploymentID() + ")";
    }

    public boolean isLoadOnStartup() {
        return getSingleton().loadOnStartup;
    }

    public void setLoadOnStartup(final boolean loadOnStartup) {
        this.getSingleton().loadOnStartup = loadOnStartup;
    }

    public Set<String> getDependsOn() {
        return getSingleton().dependsOn;
    }

    public boolean isSessionSynchronized() {
        return !isBeanManagedTransaction();
    }

    public boolean isLocalbean() {
        return localbean;
    }

    public Class getBusinessLocalBeanInterface() {
        if (this.isLocalbean()) {
            return this.beanClass;
        }
        return null;
    }

    public Duration getAccessTimeout(final Method beanMethod) {
        return getMethodContext(beanMethod).getAccessTimeout();
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getStatefulTimeout() {
        return getStateful().statefulTimeout;
    }

    public void setStatefulTimeout(final Duration statefulTimeout) {
        this.getStateful().statefulTimeout = statefulTimeout;
    }

    public Class<Object> getManagedClass() {
        if (isDynamicallyImplemented()) {
            return (Class<Object>) getProxyClass();
        }
        return this.beanClass;
    }

    @SuppressWarnings("unchecked")
    public InstanceContext newInstance() throws Exception {
        final boolean dynamicallyImplemented = isDynamicallyImplemented();

        final WebBeansContext webBeansContext = getWebBeansContext();

        if (dynamicallyImplemented) {
            if (!InvocationHandler.class.isAssignableFrom(getProxyClass())) {
                throw new OpenEJBException("proxy class can only be InvocationHandler");
            }
        }

        final ThreadContext callContext = new ThreadContext(this, null, Operation.INJECTION);
        final ThreadContext oldContext = ThreadContext.enter(callContext);

        try {
            final Context ctx = getJndiEnc();
            final Class beanClass = getBeanClass();

            final CurrentCreationalContext<Object> currentCreationalContext = get(CurrentCreationalContext.class);
            CreationalContext<Object> creationalContext = currentCreationalContext != null ? currentCreationalContext.get() : null;

            final CdiEjbBean cdiEjbBean = get(CdiEjbBean.class);

            if (!CreationalContextImpl.class.isInstance(creationalContext) && webBeansContext != null) {
                if (creationalContext == null) {
                    creationalContext = webBeansContext.getCreationalContextFactory().getCreationalContext(cdiEjbBean);
                } else {
                    creationalContext = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(creationalContext, cdiEjbBean);
                }
            }

            final Object rootInstance;
            if (cdiEjbBean != null && !dynamicallyImplemented && CdiEjbBean.EjbInjectionTargetImpl.class.isInstance(cdiEjbBean.getInjectionTarget())) {
                rootInstance = CdiEjbBean.EjbInjectionTargetImpl.class.cast(cdiEjbBean.getInjectionTarget()).createNewPojo(creationalContext);
            } else { // not a cdi bean
                rootInstance = getManagedClass().newInstance();
            }

            // Create bean instance
            Object beanInstance;

            final InjectionProcessor injectionProcessor;
            if (!dynamicallyImplemented) {
                injectionProcessor = new InjectionProcessor(rootInstance, getInjections(), InjectionProcessor.unwrap(ctx));
                beanInstance = injectionProcessor.createInstance();
                inject(beanInstance, creationalContext);
            } else {
                // update target
                final List<Injection> newInjections = new ArrayList<>();
                for (final Injection injection : getInjections()) {
                    if (beanClass.equals(injection.getTarget())) {
                        final Injection updated = new Injection(injection.getJndiName(), injection.getName(), proxyClass);
                        newInjections.add(updated);
                    } else {
                        newInjections.add(injection);
                    }
                }
                injections.clear();
                injections.addAll(newInjections);

                injectionProcessor = new InjectionProcessor(rootInstance, injections, InjectionProcessor.unwrap(ctx));
                final InvocationHandler handler = (InvocationHandler) injectionProcessor.createInstance();
                beanInstance = DynamicProxyImplFactory.newProxy(this, handler);
                inject(handler, creationalContext);
            }

            // Create interceptors
            final Map<String, Object> interceptorInstances = new LinkedHashMap<>();

            // Add the stats interceptor instance and other already created interceptor instances
            for (final InterceptorInstance interceptorInstance : this.getUserAndSystemInterceptors()) {
                final Class clazz = interceptorInstance.getData().getInterceptorClass();
                interceptorInstances.put(clazz.getName(), interceptorInstance.getInterceptor());
            }

            final Collection<DependentCreationalContext<?>> createdDependents = getDependents(creationalContext);
            for (final InterceptorData interceptorData : this.getInstanceScopedInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) {
                    continue;
                }

                final Class clazz = interceptorData.getInterceptorClass();

                final Object iInstance;
                if (webBeansContext != null) {
                    Object preInstantiated = null;
                    if (createdDependents != null) {
                        for (final DependentCreationalContext<?> dcc : createdDependents) {
                            if (clazz.isInstance(dcc.getInstance())) { // is that enough? do we have more to match?
                                preInstantiated = dcc.getInstance();
                                break;
                            }
                        }
                    }
                    if (preInstantiated != null) {
                        iInstance = preInstantiated;
                    } else {
                        ConstructorInjectionBean interceptorConstructor = interceptorData.get(ConstructorInjectionBean.class);
                        if (interceptorConstructor == null) {
                            synchronized (this) {
                                interceptorConstructor = interceptorData.get(ConstructorInjectionBean.class);
                                if (interceptorConstructor == null) {
                                    interceptorConstructor = new ConstructorInjectionBean(webBeansContext, clazz, webBeansContext.getAnnotatedElementFactory().newAnnotatedType(clazz));
                                    interceptorData.set(ConstructorInjectionBean.class, interceptorConstructor);
                                }
                            }
                        }
                        CreationalContextImpl cc = (CreationalContextImpl) creationalContext;
                        Object oldDelegate = cc.putDelegate(beanInstance);
                        Bean<?> oldBean = cc.putBean(cdiEjbBean);
                        Contextual<?> oldContextual = cc.putContextual(interceptorData.getCdiInterceptorBean() != null
                                ? interceptorData.getCdiInterceptorBean()
                                : interceptorConstructor); // otherwise BeanMetaData is broken

                        try {
                            iInstance = interceptorConstructor.create(creationalContext);
                        }
                        finally {
                            cc.putBean(oldBean);
                            cc.putContextual(oldContextual);
                            cc.putDelegate(oldDelegate);
                        }

                    }
                } else {
                    iInstance = clazz.newInstance();
                }

                final InjectionProcessor interceptorInjector = new InjectionProcessor(iInstance, this.getInjections(), InjectionProcessor.unwrap(ctx));
                try {
                    final Object interceptorInstance = interceptorInjector.createInstance();
                    if (webBeansContext != null) {
                        try {
                            OWBInjector.inject(webBeansContext.getBeanManagerImpl(), interceptorInstance, creationalContext);
                        } catch (final Throwable t) {
                            // TODO handle this differently
                            // this is temporary till the injector can be rewritten
                        }
                    }

                    interceptorInstances.put(clazz.getName(), interceptorInstance);
                } catch (final ConstructionException e) {
                    throw new Exception("Failed to create interceptor: " + clazz.getName(), e);
                }
            }

            interceptorInstances.put(beanClass.getName(), beanInstance);

            // Invoke post construct method
            callContext.setCurrentOperation(Operation.POST_CONSTRUCT);
            final List<InterceptorData> callbackInterceptors = this.getCallbackInterceptors();
            final InterceptorStack postConstruct = new InterceptorStack(beanInstance, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);

            //Transaction Demarcation for Singleton PostConstruct method
            TransactionType transactionType;

            if (componentType == BeanType.SINGLETON || componentType == BeanType.STATEFUL) {
                final Set<Method> callbacks = callbackInterceptors.get(callbackInterceptors.size() - 1).getPostConstruct();
                if (callbacks.isEmpty()) {
                    transactionType = TransactionType.RequiresNew;
                } else {
                    transactionType = getTransactionType(callbacks.iterator().next()); // TODO: we should take the last one I think
                    if (transactionType == TransactionType.Required) {
                        transactionType = TransactionType.RequiresNew;
                    }
                }
            } else {
                transactionType = isBeanManagedTransaction() ? TransactionType.BeanManaged : TransactionType.NotSupported;
            }
            final TransactionPolicy transactionPolicy = EjbTransactionUtil.createTransactionPolicy(transactionType, callContext);
            try {
                //Call the chain
                if (cdiEjbBean != null) { // call it, it has no postconstruct but extensions can add stuff here, TODO: see if it should be called before or after effective postconstruct
                    cdiEjbBean.getInjectionTarget().postConstruct(beanInstance);
                }
                postConstruct.invoke();
            } catch (final Throwable e) {
                //RollBack Transaction
                EjbTransactionUtil.handleSystemException(transactionPolicy, e, callContext);
            } finally {
                EjbTransactionUtil.afterInvoke(transactionPolicy, callContext);
            }

            // handle cdi decorators
            if (cdiEjbBean != null) {
                final Class<?> proxyClass = Class.class.cast(Reflections.get(cdiEjbBean.getInjectionTarget(), "proxyClass"));
                if (proxyClass != null) { // means interception
                    final InterceptorResolutionService.BeanInterceptorInfo interceptorInfo = cdiEjbBean.getBeanContext().get(InterceptorResolutionService.BeanInterceptorInfo.class);
                    if (interceptorInfo.getDecorators() != null && !interceptorInfo.getDecorators().isEmpty()) {
                        final InterceptorDecoratorProxyFactory pf = webBeansContext.getInterceptorDecoratorProxyFactory();

                        // decorators
                        final Object instance = beanInstance;
                        final List<Decorator<?>> decorators = interceptorInfo.getDecorators();
                        final Map<Decorator<?>, Object> instances = new HashMap<>();
                        for (int i = decorators.size(); i > 0; i--) {
                            final Decorator<?> decorator = decorators.get(i - 1);
                            CreationalContextImpl cc = (CreationalContextImpl) creationalContext;
                            Object oldDelegate = cc.putDelegate(beanInstance);
                            Bean<?> oldBean = cc.putBean(cdiEjbBean);
                            Contextual<?> oldContextual = cc.putContextual(decorator); // otherwise BeanMetaData is broken

                            Object decoratorInstance = null;
                            try {
                                decoratorInstance = decorator.create(CreationalContext.class.cast(creationalContext));
                            }
                            finally {
                                cc.putBean(oldBean);
                                cc.putContextual(oldContextual);
                                cc.putDelegate(oldDelegate);
                            }
                            instances.put(decorator, decoratorInstance);
                            beanInstance = pf.createProxyInstance(proxyClass, instance,
                                new DecoratorHandler(interceptorInfo, decorators, instances, i - 1, instance, cdiEjbBean.getId()));
                        }
                    }
                }
            }

            return new InstanceContext(this, beanInstance, interceptorInstances, creationalContext);
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

    private Collection<DependentCreationalContext<?>> getDependents(final CreationalContext<Object> creationalContext) {
        try {
            return Collection.class.cast(DEPENDENTS_OBJECTS.get(creationalContext));
        } catch (final Exception e) {
            return emptyList();
        }
    }

    private ConstructorInjectionBean<Object> createConstructorInjectionBean(final WebBeansContext webBeansContext) {
        if (constructorInjectionBean != null) {
            return constructorInjectionBean;
        }

        synchronized (this) { // concurrentmodificationexception because of annotatedtype internals otherwise
            if (constructorInjectionBean == null) {
                constructorInjectionBean = new ConstructorInjectionBean<>(
                    webBeansContext, getManagedClass(),
                    webBeansContext.getAnnotatedElementFactory().newAnnotatedType(getManagedClass()));
            }
        }
        return constructorInjectionBean;
    }

    @SuppressWarnings("unchecked")
    public <T> void inject(final T instance, CreationalContext<T> ctx) {

        final WebBeansContext webBeansContext = getWebBeansContext();
        if (webBeansContext == null) {
            return;
        }

        InjectionTargetBean<T> beanDefinition = get(CdiEjbBean.class);

        if (beanDefinition == null) {
            beanDefinition = InjectionTargetBean.class.cast(createConstructorInjectionBean(webBeansContext));
        }

        if (!(ctx instanceof CreationalContextImpl)) {
            ctx = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(ctx, beanDefinition);
        }

        beanDefinition.getInjectionTarget().inject(instance, ctx);
    }

    public WebBeansContext getWebBeansContext() {
        final CdiEjbBean<?> bean = get(CdiEjbBean.class);
        if (bean != null) {
            return bean.getWebBeansContext();
        }
        return moduleContext.getAppContext().getWebBeansContext();
    }

    public Set<Class<?>> getAsynchronousClasses() {
        return asynchronousClasses;
    }

    public Set<String> getAsynchronousMethodSignatures() {
        return asynchronousMethodSignatures;
    }

    public void createAsynchronousMethodSet() {
        for (final Map.Entry<Method, MethodContext> entry : methodContextMap.entrySet()) {
            if (entry.getValue().isAsynchronous()) {
                asynchronousMethodSignatures.add(generateMethodSignature(entry.getKey()));
            }
        }
        for (final Class<?> cls : asynchronousClasses) {
            for (final Method method : cls.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    asynchronousMethodSignatures.add(generateMethodSignature(method));
                }
            }
        }
    }

    private String generateMethodSignature(final Method method) {
        final StringBuilder buffer = new StringBuilder(method.getName());
        for (final Class<?> parameterType : method.getParameterTypes()) {
            buffer.append(parameterType.getName());
        }
        return buffer.toString();
    }

    private MethodContext getViewMethodContext(final Method method, final String view) {
        final ViewContext viewContext = this.viewContextMap.get(view);
        return viewContext == null ? null : viewContext.getMethodContext(method);
    }

    private MethodContext initViewMethodContext(final Method method, final String view) {
        ViewContext viewContext = this.viewContextMap.get(view);
        if (viewContext == null) {
            viewContext = new ViewContext();
            viewContextMap.put(view, viewContext);
        }

        return viewContext.initMethodContext(method);
    }

    public Class<?> getProxyClass() {
        return proxyClass;
    }

    public boolean isCdiCompatible() {
        return componentType.isCdiCompatible() && !Comp.class.equals(beanClass);
    }

    public class ViewContext {

        private final Map<Method, MethodContext> methodContextMap = new HashMap<>();

        public MethodContext getMethodContext(final Method method) {
            return methodContextMap.get(method);
        }

        public MethodContext initMethodContext(final Method method) {
            MethodContext methodContext = methodContextMap.get(method);
            if (methodContext != null) {
                return methodContext;
            }

            methodContext = new MethodContext(BeanContext.this, method);
            methodContextMap.put(method, methodContext);

            return methodContext;
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isPassivatingScope() {
        final CdiEjbBean<?> bean = get(CdiEjbBean.class);
        if (bean == null) {
            return true;
        }

        if (ConversationScoped.class == bean.getScope()) {
            try {
                return !bean.getWebBeansContext().getConversationManager().getConversationBeanReference().isTransient();
            } catch (final RuntimeException re) { // conversation not found for instance so act as @RequestScoped
                return false;
            }
        }

        return true;
    }

    public boolean isPassivable() {
        return componentType == BeanType.STATEFUL && passivable;
    }

    public void stop() {
        if (ejbTimerService != null) {
            ejbTimerService.stop();
        }
    }

    public void addSecurityRoleReference(final String roleName, final String roleLink) {
        securityRoleReferences.put(roleName, roleLink);
    }

    public String getSecurityRoleReference(final String roleName) {
        final String roleLink = securityRoleReferences.get(roleName);
        return roleLink != null ? roleLink : roleName;
    }

    private static class Cmp {

        private boolean cmp2;
        private KeyGenerator keyGenerator;
        private String primaryKeyField;
        private Class cmpImplClass;
        private String abstractSchemaName;
        private Class pkClass;
        private final Set<String> remoteQueryResults = new TreeSet<>();
        private boolean isReentrant;
        private final Map<Method, Method> postCreateMethodMap = new HashMap<>();
    }

    private static class Mdb {

        private String destinationId;
        private final Map<String, String> activationProperties = new HashMap<>();
        private Class mdbInterface;
    }

    private static class Singleton {

        private LockType lockType = LockType.WRITE;
        private boolean loadOnStartup;
        private final Set<String> dependsOn = new LinkedHashSet<>();
    }

    private static class Stateful {

        private Index<EntityManagerFactory, EntityManagerConfiguration> extendedEntityManagerFactories;
        private Duration statefulTimeout;
        private final List<Method> removeMethods = new ArrayList<>();
    }

    private static class LegacyView {

        private EJBHome ejbHomeRef;
        private EJBLocalHome ejbLocalHomeRef;
        private Class homeInterface;
        private Class remoteInterface;
        private Class localHomeInterface;
        private Class localInterface;
        private Method createMethod;
    }

    public static final class ProxyClass {
        private final Class<?> proxy;

        public ProxyClass(final BeanContext beanContext,
                          final Class<?>[] interfaces) {
            Class<?> clazz;
            try {
                clazz = LocalBeanProxyFactory.createProxy(
                    beanContext.getBeanClass(),
                    beanContext.getClassLoader(),
                    interfaces);
            } catch (final Throwable e) { // VerifyError
                logger.debug(beanContext.getBeanClass().getName() + " is not proxiable", e);
                clazz = null;
            }
            this.proxy = clazz;
        }

        public Class<?> getProxy() {
            return proxy; // let it generate a NPE if null, shouldn't occur (tested elsewhere) excepted for test where we don't use it
        }
    }

    public static class EntityManagerConfiguration {
        private final Map properties;
        private final SynchronizationType synchronizationType;

        public EntityManagerConfiguration(final Map properties, final SynchronizationType synchronizationType) {
            this.properties = properties;
            this.synchronizationType = synchronizationType;
        }

        public Map getProperties() {
            return properties;
        }

        public SynchronizationType getSynchronizationType() {
            return synchronizationType;
        }
    }
}
