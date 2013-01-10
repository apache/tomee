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
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.transaction.EjbTransactionUtil;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.DynamicProxyImplFactory;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.xbean.recipe.ConstructionException;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.MessageDrivenBean;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import java.util.concurrent.Future;

@SuppressWarnings("unchecked")
public class BeanContext extends DeploymentContext {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, BeanContext.class);

    public static final String USER_INTERCEPTOR_KEY = "org.apache.openejb.default.system.interceptors";
    public static final String USER_INTERCEPTOR_SEPARATOR = ",| |;";

    private boolean isPassivatingScope = true;

    public boolean isDynamicallyImplemented() {
        return proxyClass != null;
    }

    public interface BusinessLocalHome extends javax.ejb.EJBLocalHome {

        Object create();
    }

    public interface BusinessLocalBeanHome extends javax.ejb.EJBLocalHome {

        Object create();
    }

    public interface BusinessRemoteHome extends javax.ejb.EJBHome {

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
    private final List<Class> businessLocals = new ArrayList<Class>();
    private final List<Class> businessRemotes = new ArrayList<Class>();
    private Class serviceEndpointInterface;

    private Method ejbTimeout;
    private EjbTimerService ejbTimerService;

    private boolean isBeanManagedTransaction;
    private boolean isBeanManagedConcurrency;
    private Container container;

    private String ejbName;
    private String runAs;

    private final BeanType componentType;

    private boolean hidden = false;

    //private final Map<Method, TransactionType> methodTransactionType = new HashMap<Method, TransactionType>();
    private final Map<Method, Method> methodMap = new HashMap<Method, Method>();
    private final Map<Method, MethodContext> methodContextMap = new HashMap<Method, MethodContext>();
    private final Map<String, ViewContext> viewContextMap = new HashMap<String, ViewContext>();

    private TransactionPolicyFactory transactionPolicyFactory;

    private final List<InterceptorData> callbackInterceptors = new ArrayList<InterceptorData>();
    private final Set<InterceptorData> instanceScopedInterceptors = new HashSet<InterceptorData>();
    private final List<InterceptorInstance> systemInterceptors = new ArrayList<InterceptorInstance>();
    private final List<InterceptorInstance> userInterceptors = new ArrayList<InterceptorInstance>();
    private final List<Injection> injections = new ArrayList<Injection>();
    private final Map<Class, InterfaceType> interfaces = new HashMap<Class, InterfaceType>();
    private final Map<Class, ExceptionType> exceptions = new HashMap<Class, ExceptionType>();

    private final boolean localbean;
    private Duration accessTimeout;

    private Set<Class<?>> asynchronousClasses = new HashSet<Class<?>>();
    private Set<String> asynchronousMethodSignatures = new HashSet<String>();
    private Class<?> proxyClass;

    private Mdb mdb;
    private Singleton singleton;
    private Stateful stateful;
    private Cmp cmp;
    private LegacyView legacyView;

    /**
     * TODO: Move to MethodContext
     */
    private final Map<Method, Boolean> removeExceptionPolicy = new HashMap<Method, Boolean>();

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
                final List<Class> interfaces = new ArrayList<Class>();
                interfaces.add(getInterface(interfaceType));
                return interfaces;
        }
    }

    public InterfaceType getInterfaceType(final Class clazz) {
        final InterfaceType type = interfaces.get(clazz);
        if (type != null)
            return type;

        if (javax.ejb.EJBLocalHome.class.isAssignableFrom(clazz))
            return InterfaceType.EJB_LOCAL_HOME;
        if (javax.ejb.EJBLocalObject.class.isAssignableFrom(clazz))
            return InterfaceType.EJB_LOCAL;
        if (javax.ejb.EJBHome.class.isAssignableFrom(clazz))
            return InterfaceType.EJB_HOME;
        if (javax.ejb.EJBObject.class.isAssignableFrom(clazz))
            return InterfaceType.EJB_OBJECT;

        return null;
    }

    /**
     * load default interceptors configured in properties.
     */
    private BeanContext(final String id, final Context jndiContext, final ModuleContext moduleContext, final BeanType componentType, final boolean localBean, final Class beanClass) {
        super(id, moduleContext.getOptions());

        if (beanClass == null) {
            throw new NullPointerException("beanClass input parameter is null");
        }

        this.moduleContext = moduleContext;
        this.jndiContext = new ContextHandler(jndiContext);
        this.localbean = localBean;
        this.componentType = componentType;
        this.beanClass = beanClass;

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
                    } catch (Exception e) {
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
                       final boolean localBean) throws SystemException {
        this(id, jndiContext, moduleContext, componentType, localBean, beanClass);

        this.proxyClass = proxy;

        if (homeInterface != null)
            this.getLegacyView().homeInterface = homeInterface;
        if (localInterface != null)
            this.getLegacyView().localInterface = localInterface;
        if (localHomeInterface != null)
            this.getLegacyView().localHomeInterface = localHomeInterface;
        if (remoteInterface != null)
            this.getLegacyView().remoteInterface = remoteInterface;

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
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        addInterface(getServiceEndpointInterface(), InterfaceType.SERVICE_ENDPOINT);

        addInterface(javax.ejb.EJBHome.class, InterfaceType.EJB_HOME);
        addInterface(javax.ejb.EJBObject.class, InterfaceType.EJB_OBJECT);

        addInterface(javax.ejb.EJBLocalHome.class, InterfaceType.EJB_LOCAL_HOME);
        addInterface(javax.ejb.EJBLocalObject.class, InterfaceType.EJB_LOCAL);

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
        if (interfce == null)
            return;
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
        if (e instanceof RuntimeException) {
            return ExceptionType.SYSTEM;
        } else {
            return ExceptionType.APPLICATION;
        }
    }

    public BeanContext(final String id, final Context jndiContext, final ModuleContext moduleContext, final Class beanClass, final Class mdbInterface, final Map<String, String> activationProperties) throws SystemException {
        this(id, jndiContext, moduleContext, BeanType.MESSAGE_DRIVEN, false, beanClass);

        this.getMdb().mdbInterface = mdbInterface;
        this.getMdb().activationProperties.putAll(activationProperties);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout", Timer.class);
            } catch (NoSuchMethodException e) {
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

        final ArrayList<Class> classes = new ArrayList<Class>();

        for (final Class local : businessRemotes) {
            classes.add(local);
        }

        for (final Class local : businessLocals) {
            classes.add(local);
        }

        classes.add(this.beanClass);

        for (final Class c : classes) {
            Lock lock = null;
            try {

                lock = (Lock) c.getAnnotation(Lock.class);
                this.getSingleton().lockType = lock.value();

                if (logger.isDebugEnabled()) {
                    logger.debug("Declared Lock for " + c.getName() + " is " + this.getSingleton().lockType);
                }

            } catch (NullPointerException e) {
                //Ignore
            } catch (Throwable e) {
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

    public Index<EntityManagerFactory, Map> getExtendedEntityManagerFactories() {
        return getStateful().extendedEntityManagerFactories;
    }

    public void setExtendedEntityManagerFactories(final Index<EntityManagerFactory, Map> extendedEntityManagerFactories) {
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

        if (methodContext == null)
            methodContext = methodContextMap.get(method);

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
        return (isBeanManagedTransaction) ? TransactionType.BeanManaged : TransactionType.Required;
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
        return (legacyView == null) ? null : getLegacyView().homeInterface;
    }

    public Class getRemoteInterface() {
        return (legacyView == null) ? null : getLegacyView().remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return (legacyView == null) ? null : getLegacyView().localHomeInterface;
    }

    public Class getLocalInterface() {
        return (legacyView == null) ? null : getLegacyView().localInterface;
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
        return (mdb == null) ? null : getMdb().mdbInterface;
    }

    public Class getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public Map<String, String> getActivationProperties() {
        return getMdb().activationProperties;
    }

    public Class getPrimaryKeyClass() {
        return (cmp == null) ? null : cmp.pkClass;
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
        final List<Class> interfaces = new ArrayList<Class>();
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
            if (!getBusinessLocalInterfaces().contains(clazz)) {
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
        return (method == null) ? interfaceMethod : method;
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
     * <p/>
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
        final List<InterceptorInstance> interceptors = new ArrayList<InterceptorInstance>(systemInterceptors);
        interceptors.addAll(userInterceptors);
        return interceptors;
    }

    private final Set<InterceptorData> cdiInterceptors = new LinkedHashSet<InterceptorData>();

    public List<InterceptorData> getCallbackInterceptors() {
        final List<InterceptorData> datas = getInterceptorData();
        datas.addAll(callbackInterceptors);
        datas.addAll(cdiInterceptors);
        return datas;
    }

    public void setCallbackInterceptors(final List<InterceptorData> callbackInterceptors) {
        //TODO shouldn't we remove the old callbackInterceptors from instanceScopedInterceptors before adding the new ones?
        this.callbackInterceptors.clear();
        this.callbackInterceptors.addAll(callbackInterceptors);
        this.instanceScopedInterceptors.addAll(callbackInterceptors);
    }

    public List<InterceptorData> getCdiInterceptors() {
        return new ArrayList<InterceptorData>(cdiInterceptors);
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
        final List<InterceptorData> datas = new ArrayList<InterceptorData>();
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

    public void createMethodMap() throws org.apache.openejb.SystemException {
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
            } catch (NoSuchMethodException e) {
                // if there isn't an ejbCreate method that is fine
            }
        }

        try {
            // map the remove methods
            if (componentType == BeanType.STATEFUL || componentType == BeanType.MANAGED) {

                Method beanMethod = null;
                if (javax.ejb.SessionBean.class.isAssignableFrom(beanClass)) {
                    beanMethod = javax.ejb.SessionBean.class.getDeclaredMethod("ejbRemove");
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

                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", javax.ejb.Handle.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", java.lang.Object.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBLocalObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
            } else if (componentType == BeanType.BMP_ENTITY || componentType == BeanType.CMP_ENTITY) {
                final Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove");
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", javax.ejb.Handle.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", java.lang.Object.class);
                mapMethods(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBLocalObject.class.getDeclaredMethod("remove");
                mapMethods(clientMethod, beanMethod);
            }
        } catch (java.lang.NoSuchMethodException nsme) {
            throw new org.apache.openejb.SystemException(nsme);
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
                        if (getCmp().cmpImplClass != null)
                            clazz = getCmp().cmpImplClass;
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
            } catch (NoSuchMethodException nsme) {
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
            } catch (NoSuchMethodException nsme) {
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
            final List<Class> classes = new ArrayList<Class>();
            classes.add(getLocalInterface());
            return classes;
        } else if (EJBHome.class.isAssignableFrom(homeInterface)) {
            final List<Class> classes = new ArrayList<Class>();
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

    public void setEjbName(final String ejbName) {
        this.ejbName = ejbName;
    }

    public void setRunAs(final String runAs) {
        this.runAs = runAs;
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
        final ThreadContext callContext = new ThreadContext(this, null, Operation.INJECTION);
        final ThreadContext oldContext = ThreadContext.enter(callContext);

        final WebBeansContext webBeansContext = getModuleContext().getAppContext().getWebBeansContext();
        AbstractInjectionTargetBean<Object> beanDefinition = get(CdiEjbBean.class);

        if (isDynamicallyImplemented()) {
            if (!InvocationHandler.class.isAssignableFrom(getProxyClass())) {
                throw new OpenEJBException("proxy class can only be InvocationHandler");
            }
        }

        final ConstructorInjectionBean<Object> beanConstructor = new ConstructorInjectionBean<Object>(webBeansContext, getManagedClass());
        if (beanDefinition == null) {
            beanDefinition = beanConstructor;
        }

        try {
            final Context ctx = this.getJndiEnc();
            final Class beanClass = this.getBeanClass();

            final CurrentCreationalContext<Object> currentCreationalContext = get(CurrentCreationalContext.class);
            CreationalContext<Object> creationalContext = (currentCreationalContext != null) ? currentCreationalContext.get() : null;

            if (creationalContext == null) {
                creationalContext = webBeansContext.getBeanManagerImpl().createCreationalContext(beanDefinition);
            }

            // Create bean instance
            final Object beanInstance;
            final InjectionProcessor injectionProcessor;
            if (!isDynamicallyImplemented()) {
                injectionProcessor = new InjectionProcessor(beanConstructor.create(creationalContext), getInjections(), InjectionProcessor.unwrap(ctx));
                beanInstance = injectionProcessor.createInstance();
                inject(beanInstance, creationalContext);
            } else {
                // update target
                final List<Injection> newInjections = new ArrayList<Injection>();
                for (final Injection injection : getInjections()) {
                    if (beanClass.equals(injection.getTarget())) {
                        final Injection updated = new Injection(injection.getJndiName(), injection.getName(), beanDefinition.getBeanClass());
                        newInjections.add(updated);
                    } else {
                        newInjections.add(injection);
                    }
                }
                injections.clear();
                injections.addAll(newInjections);

                injectionProcessor = new InjectionProcessor(beanConstructor.create(creationalContext), injections, InjectionProcessor.unwrap(ctx));
                final InvocationHandler handler = (InvocationHandler) injectionProcessor.createInstance();
                beanInstance = DynamicProxyImplFactory.newProxy(this, handler);
                inject(handler, creationalContext);
            }

            // Create interceptors
            final Map<String, Object> interceptorInstances = new LinkedHashMap<String, java.lang.Object>();

            // Add the stats interceptor instance and other already created interceptor instances
            for (final InterceptorInstance interceptorInstance : this.getUserAndSystemInterceptors()) {
                final Class clazz = interceptorInstance.getData().getInterceptorClass();
                interceptorInstances.put(clazz.getName(), interceptorInstance.getInterceptor());
            }

            for (final InterceptorData interceptorData : this.getInstanceScopedInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) {
                    continue;
                }

                final Class clazz = interceptorData.getInterceptorClass();

                final ConstructorInjectionBean interceptorConstructor = new ConstructorInjectionBean(webBeansContext, clazz);
                final InjectionProcessor interceptorInjector = new InjectionProcessor(interceptorConstructor.create(creationalContext), this.getInjections(), org.apache.openejb.InjectionProcessor.unwrap(ctx));
                try {
                    final Object interceptorInstance = interceptorInjector.createInstance();
                    try {
                        final Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();
                        AbstractInjectable.instanceUnderInjection.set(interceptorInstance);
                        try {
                            OWBInjector.inject(webBeansContext.getBeanManagerImpl(), interceptorInstance, creationalContext);
                        } finally {
                            if (oldInstanceUnderInjection != null) {
                                AbstractInjectable.instanceUnderInjection.set(oldInstanceUnderInjection);
                            } else {
                                AbstractInjectable.instanceUnderInjection.remove();
                            }
                        }
                    } catch (Throwable t) {
                        // TODO handle this differently
                        // this is temporary till the injector can be rewritten
                    }

                    interceptorInstances.put(clazz.getName(), interceptorInstance);
                } catch (ConstructionException e) {
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

            if (getComponentType() == BeanType.SINGLETON) {
                final Set<Method> callbacks = callbackInterceptors.get(callbackInterceptors.size() - 1).getPostConstruct();
                if (callbacks.isEmpty()) {
                    transactionType = TransactionType.RequiresNew;
                } else {
                    transactionType = getTransactionType(callbacks.iterator().next());
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
                postConstruct.invoke();
            } catch (Throwable e) {
                //RollBack Transaction
                EjbTransactionUtil.handleSystemException(transactionPolicy, e, callContext);
            } finally {
                EjbTransactionUtil.afterInvoke(transactionPolicy, callContext);
            }

            return new InstanceContext(this, beanInstance, interceptorInstances, creationalContext);
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

    protected <X> X getBean(final Class<X> clazz, final Bean<?> bean) {
        return clazz.cast(bean);
    }

    @SuppressWarnings("unchecked")
    public <T> void inject(final T instance, CreationalContext<T> ctx) {

        final WebBeansContext webBeansContext = getModuleContext().getAppContext().getWebBeansContext();

        AbstractInjectionTargetBean<Object> beanDefinition = get(CdiEjbBean.class);

        final ConstructorInjectionBean<Object> beanConstructor = new ConstructorInjectionBean<Object>(webBeansContext, getManagedClass());

        if (beanDefinition == null) {
            beanDefinition = beanConstructor;
        }

        if (!(ctx instanceof CreationalContextImpl)) {
            ctx = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(ctx, beanDefinition);
        }

        final Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();
        boolean isInjectionToAnotherBean = false;
        try {
            if (ctx instanceof CreationalContextImpl) {
                final Contextual<?> contextual = ((CreationalContextImpl) ctx).getBean();
                isInjectionToAnotherBean = contextual != getBean(InjectionTargetBean.class, beanDefinition);
            }

            if (!isInjectionToAnotherBean) {
                AbstractInjectable.instanceUnderInjection.set(instance);
            }

            final InjectionTargetBean<T> bean = getBean(InjectionTargetBean.class, beanDefinition);

            bean.injectResources(instance, ctx);
            bean.injectSuperFields(instance, ctx);
            bean.injectSuperMethods(instance, ctx);
            bean.injectFields(instance, ctx);
            bean.injectMethods(instance, ctx);
        } finally {
            if (oldInstanceUnderInjection != null) {
                AbstractInjectable.instanceUnderInjection.set(oldInstanceUnderInjection);
            } else {
                AbstractInjectable.instanceUnderInjection.set(null);
                AbstractInjectable.instanceUnderInjection.remove();
            }
        }

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
        return (viewContext == null) ? null : viewContext.getMethodContext(method);
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

    public boolean isCdiCompatible(){
        return componentType.isCdiCompatible() && !Comp.class.equals(beanClass);
    }

    public class ViewContext {

        private final Map<Method, MethodContext> methodContextMap = new HashMap<Method, MethodContext>();

        public MethodContext getMethodContext(final Method method) {
            return methodContextMap.get(method);
        }

        public MethodContext initMethodContext(final Method method) {
            MethodContext methodContext = methodContextMap.get(method);
            if (methodContext != null)
                return methodContext;

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

    public void initIsPassivationScope() {
        // CDI 6.6.4
        if (BeanType.STATELESS.equals(componentType) || BeanType.SINGLETON.equals(componentType)) {
            isPassivatingScope = false;
            return;
        }

        final BeanManagerImpl bm = moduleContext.getAppContext().getWebBeansContext().getBeanManagerImpl();
        if (!bm.isInUse()) {
            isPassivatingScope = true;
            return;
        }

        final CdiEjbBean<?> bean = get(CdiEjbBean.class);
        if (bean == null) {
            isPassivatingScope = true;
            return;
        }

        final Class<? extends Annotation> scope = bean.getScope();
        isPassivatingScope = !bm.isNormalScope(scope) || bm.isPassivatingScope(scope);
    }

    public boolean isPassivatingScope() {
        final CdiEjbBean<?> bean = get(CdiEjbBean.class);
        if (bean == null) {
            return isPassivatingScope;
        }

        if (ConversationScoped.class == bean.getScope()) {
            try {
                return !bean.getWebBeansContext().getConversationManager().getConversationBeanReference().isTransient();
            } catch (RuntimeException re) { // conversation not found for instance so act as @RequestScoped
                return false;
            }
        }

        return isPassivatingScope;
    }

    public void stop() {
        if (ejbTimerService != null && ejbTimerService instanceof EjbTimerServiceImpl) {
            ((EjbTimerServiceImpl) ejbTimerService).shutdownMe();
        }
    }

    private static class Cmp {

        private boolean cmp2;
        private KeyGenerator keyGenerator;
        private String primaryKeyField;
        private Class cmpImplClass;
        private String abstractSchemaName;
        private Class pkClass;
        private Set<String> remoteQueryResults = new TreeSet<String>();
        private boolean isReentrant;
        private final Map<Method, Method> postCreateMethodMap = new HashMap<Method, Method>();
    }

    private static class Mdb {

        private String destinationId;
        private final Map<String, String> activationProperties = new HashMap<String, String>();
        private Class mdbInterface;
    }

    private static class Singleton {

        private LockType lockType = LockType.WRITE;
        private boolean loadOnStartup;
        private final Set<String> dependsOn = new LinkedHashSet<String>();
    }

    private static class Stateful {

        private Index<EntityManagerFactory, Map> extendedEntityManagerFactories;
        private Duration statefulTimeout;
        private final List<Method> removeMethods = new ArrayList<Method>();
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

    private static class ContextHandler extends ContextWrapper {

        public ContextHandler(final Context jndiContext) {
            super(jndiContext);
        }

        @Override
        public Object lookup(final Name name) throws NamingException {
            try {
                return context.lookup(name);
            } catch (NameNotFoundException nnfe) {
                try {
                    return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(name);
                } catch (NameNotFoundException nnfe2) {
                    // ignore, let it be thrown
                }
                throw nnfe;
            }
        }

        @Override
        public Object lookup(String name) throws NamingException {
            try {
                return context.lookup(name);
            } catch (NameNotFoundException nnfe) {
                try {
                    return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(name);
                } catch (NameNotFoundException nnfe2) {
                    // ignore, let it be thrown
                }
                throw nnfe;
            }
        }
    }
}
