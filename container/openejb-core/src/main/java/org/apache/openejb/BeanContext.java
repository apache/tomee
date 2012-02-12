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
import org.apache.openejb.cdi.OWBInjector;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.interceptor.InterceptorStack;
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
import org.apache.openejb.util.proxy.QueryProxy;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.xbean.recipe.ConstructionException;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.LockType;
import javax.ejb.MessageDrivenBean;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.naming.Context;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;


public class BeanContext extends DeploymentContext {
    private Logger logger = Logger.getInstance(LogCategory.OPENEJB, BeanContext.class);

    public static final String USER_INTERCEPTOR_KEY = "org.apache.openejb.default.system.interceptors";
    public static final String USER_INTERCEPTOR_SEPARATOR = ",| |;";

    public boolean isDynamicallyImplemented() {
        return getBeanClass().equals(getLocalInterface());
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
        public static String openejbCompName(String module) {
            return module + "." + Comp.class.getSimpleName();
        }
    }
    
    private final ModuleContext moduleContext;
    private final Context jndiContext;
    private Object containerData;

    private boolean destroyed;
    private Class homeInterface;
    private Class remoteInterface;
    private Class localHomeInterface;
    private Class localInterface;
    private Class beanClass;
    private Class pkClass;
    private final List<Class> businessLocals = new ArrayList<Class>();
    private final List<Class> businessRemotes = new ArrayList<Class>();
    private Class mdbInterface;
    private Class serviceEndpointInterface;

    private final List<Method> removeMethods = new ArrayList<Method>();

    private final Set<String> dependsOn = new LinkedHashSet<String>();

    private Method ejbTimeout;
    private EjbTimerService ejbTimerService;

    private boolean isBeanManagedTransaction;
    private boolean isBeanManagedConcurrency;
    private boolean isReentrant;
    private Container container;
    private EJBHome ejbHomeRef;
    private EJBLocalHome ejbLocalHomeRef;
    private String destinationId;

    private String ejbName;
    private String runAs;

    private Method createMethod = null;

    private final BeanType componentType;

    private boolean hidden = false;

    private final Map<Method, Method> postCreateMethodMap = new HashMap<Method, Method>();
    private final Map<Method, TransactionType> methodTransactionType = new HashMap<Method, TransactionType>();
    private final Map<Method, Method> methodMap = new HashMap<Method, Method>();
    private final Map<Method, MethodContext> methodContextMap = new HashMap<Method, MethodContext>();
    private final Map<String, ViewContext> viewContextMap = new HashMap<String, ViewContext>();

    private Index<EntityManagerFactory,Map> extendedEntityManagerFactories;

    private TransactionPolicyFactory transactionPolicyFactory;

    private final List<InterceptorData> callbackInterceptors = new ArrayList<InterceptorData>();
    private final Set<InterceptorData> instanceScopedInterceptors = new HashSet<InterceptorData>();
    private final List<InterceptorInstance> systemInterceptors = new ArrayList<InterceptorInstance>();
    private final List<InterceptorInstance> userInterceptors = new ArrayList<InterceptorInstance>();
    private final Map<String, String> activationProperties = new HashMap<String, String>();
    private final List<Injection> injections = new ArrayList<Injection>();
    private final Map<Class, InterfaceType> interfaces = new HashMap<Class, InterfaceType>();
    private final Map<Class, ExceptionType> exceptions = new HashMap<Class, ExceptionType>();

    private boolean loadOnStartup;
    private final boolean localbean;
    private Duration accessTimeout;
    private Duration statefulTimeout;

    private Set<Class<?>> asynchronousClasses =  new HashSet<Class<?>>();
    private Set<String> asynchronousMethodSignatures = new HashSet<String>();
    private Class<?> proxyClass;

    public Class getInterface(InterfaceType interfaceType) {
        switch(interfaceType){
            case EJB_HOME: return getHomeInterface();
            case EJB_OBJECT: return getRemoteInterface();
            case EJB_LOCAL_HOME: return getLocalHomeInterface();
            case EJB_LOCAL: return getLocalInterface();
            case BUSINESS_LOCAL: return getBusinessLocalInterface();
            case BUSINESS_REMOTE: return getBusinessRemoteInterface();
            case TIMEOUT : return BeanContext.Timeout.class;
            case BUSINESS_REMOTE_HOME: return BeanContext.BusinessRemoteHome.class;
            case BUSINESS_LOCAL_HOME: return BeanContext.BusinessLocalHome.class;
            case SERVICE_ENDPOINT: return getServiceEndpointInterface();
            case LOCALBEAN: return getBeanClass();
            case BUSINESS_LOCALBEAN_HOME: return BeanContext.BusinessLocalBeanHome.class;
            default: throw new IllegalStateException("Unexpected enum constant: " + interfaceType);
        }
    }

    public List<Class> getInterfaces(InterfaceType interfaceType) {
        switch(interfaceType){
            case BUSINESS_REMOTE: return getBusinessRemoteInterfaces();
            case BUSINESS_LOCAL: return getBusinessLocalInterfaces();
            default:
            List<Class> interfaces = new ArrayList<Class>();
            interfaces.add(getInterface(interfaceType));
            return interfaces;
        }
    }

    public InterfaceType getInterfaceType(Class clazz) {
        InterfaceType type = interfaces.get(clazz);
        if (type != null) return type;

        if (javax.ejb.EJBLocalHome.class.isAssignableFrom(clazz)) return InterfaceType.EJB_LOCAL_HOME;
        if (javax.ejb.EJBLocalObject.class.isAssignableFrom(clazz)) return InterfaceType.EJB_LOCAL;
        if (javax.ejb.EJBHome.class.isAssignableFrom(clazz)) return InterfaceType.EJB_HOME;
        if (javax.ejb.EJBObject.class.isAssignableFrom(clazz)) return InterfaceType.EJB_OBJECT;

        return null;
    }

    /**
     * load default interceptors configured in properties.
     */
    private BeanContext(String id, Context jndiContext, ModuleContext moduleContext, BeanType componentType, boolean localBean) {
        super(id, moduleContext.getOptions());
        this.moduleContext = moduleContext;
        this.jndiContext = jndiContext;
        this.localbean = localBean;
        this.componentType = componentType;

        String interceptors = SystemInstance.get().getProperties().getProperty(USER_INTERCEPTOR_KEY);
        if (interceptors != null) {
            String[] interceptorArray = interceptors.split(USER_INTERCEPTOR_SEPARATOR);
            ClassLoader classLoader = moduleContext.getClassLoader();
            for (String interceptor : interceptorArray) {
                if (interceptor != null && !interceptor.isEmpty()) {
                    Object interceptorObject;
                    try {
                        Class<?> clazz = classLoader.loadClass(interceptor);
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

    public BeanContext(String id, Context jndiContext, ModuleContext moduleContext,
                              Class beanClass, Class homeInterface,
                              Class remoteInterface,
                              Class localHomeInterface,
                              Class localInterface,
                              Class proxy,
                              Class serviceEndpointInterface, List<Class> businessLocals, List<Class> businessRemotes, Class pkClass,
                              BeanType componentType,
                              boolean localBean) throws SystemException {
        this(id, jndiContext, moduleContext, componentType, localBean);

        if (beanClass == null) throw new NullPointerException("beanClass input parameter is null");
        this.pkClass = pkClass;

        proxyClass = proxy;

        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localInterface = localInterface;
        this.localHomeInterface = localHomeInterface;
        if (businessLocals != null){
            this.businessLocals.addAll(businessLocals);
        }
        if (businessRemotes != null) {
            this.businessRemotes.addAll(businessRemotes);
        }
        this.remoteInterface = remoteInterface;
        this.beanClass = beanClass;
        this.pkClass = pkClass;
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
        for (Class businessRemote : this.businessRemotes) {
            addInterface(businessRemote, InterfaceType.BUSINESS_REMOTE);
        }

        addInterface(BeanContext.BusinessLocalHome.class, InterfaceType.BUSINESS_LOCAL_HOME);
        for (Class businessLocal : this.businessLocals) {
            addInterface(businessLocal, InterfaceType.BUSINESS_LOCAL);
        }
        if (localBean) {
            addInterface(beanClass, InterfaceType.LOCALBEAN);
        }
    }

    /**
     * DMB: This is a not so reliable way to determine the proxy type
     * The proxy type really should come with the call in the invoke.
     *
     * @param interfce
     * @param type
     */
    private void addInterface(Class interfce, InterfaceType type){
        if (interfce == null) return;
        interfaces.put(interfce, type);

        for (Class clazz : interfce.getInterfaces()) {
            addInterface(clazz, type);
        }
    }

    public void addApplicationException(Class exception, boolean rollback, boolean inherited) {
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

    public ExceptionType getExceptionType(Throwable e) {
        // Errors are always system exceptions
        if (!(e instanceof Exception)) {
            return ExceptionType.SYSTEM;
        }

        // check the registered app exceptions
        Class<?> exceptionClass = e.getClass();
        boolean inherited = false;
        while (exceptionClass != Object.class) {
            ExceptionType type = exceptions.get(exceptionClass);
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

    public BeanContext(String id, Context jndiContext, ModuleContext moduleContext, Class beanClass, Class mdbInterface, Map<String, String> activationProperties) throws SystemException {
        this(id, jndiContext, moduleContext, BeanType.MESSAGE_DRIVEN, false);
        this.beanClass = beanClass;
        this.mdbInterface = mdbInterface;
        this.activationProperties.putAll(activationProperties);

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout", Timer.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
        createMethodMap();
    }


    public Object getContainerData() {
        return containerData;
    }

    public void setContainerData(Object containerData) {
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

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public List<Injection> getInjections() {
        return injections;
    }

    public Index<EntityManagerFactory,Map> getExtendedEntityManagerFactories() {
        return extendedEntityManagerFactories;
    }

    public void setExtendedEntityManagerFactories(Index<EntityManagerFactory, Map> extendedEntityManagerFactories) {
        this.extendedEntityManagerFactories = extendedEntityManagerFactories;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public BeanType getComponentType() {
        return componentType;
    }

    public LockType getConcurrencyAttribute(Method beanMethod) {
        return getMethodContext(beanMethod).getLockType();
    }

    public LockType getLockType() {
        return LockType.WRITE;
    }

    public TransactionType getTransactionType(Method method) {
        return getTransactionType(method, null);
    }

    public TransactionType getTransactionType(Method method, InterfaceType interfaceType) {

        MethodContext methodContext = null;

        if (interfaceType != null) {
            methodContext = getViewMethodContext(method, interfaceType.getSpecName());
        }

        if (methodContext == null) methodContext = methodContextMap.get(method);

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

    public void setTransactionPolicyFactory(TransactionPolicyFactory transactionPolicyFactory) {
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
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
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
        return mdbInterface;
    }

    public Class getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public Map<String, String> getActivationProperties() {
        return activationProperties;
    }

    public Class getPrimaryKeyClass() {
        return pkClass;
    }

    public EJBHome getEJBHome() {
        if (getHomeInterface() == null) {
            throw new IllegalStateException("This component has no home interface: " + getDeploymentID());
        }
        if (ejbHomeRef == null) {
            ejbHomeRef = (EJBHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.EJB_HOME);
        }
        return ejbHomeRef;
    }


    public EJBLocalHome getEJBLocalHome() {
        if (getLocalHomeInterface() == null) {
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (ejbLocalHomeRef == null) {
            ejbLocalHomeRef = (EJBLocalHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.EJB_LOCAL_HOME);
        }
        return ejbLocalHomeRef;
    }

    //unused
    public BusinessLocalHome getBusinessLocalHome() {
        return getBusinessLocalHome(getBusinessLocalInterfaces(), null);
    }

    public BusinessLocalBeanHome getBusinessLocalBeanHome() {
        List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(this.beanClass);
        return (BusinessLocalBeanHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCALBEAN_HOME, interfaces, this.beanClass);
    }

    public BusinessLocalHome getBusinessLocalHome(Class mainInterface) {
        List<Class> localInterfaces = this.getBusinessLocalInterfaces();

        List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(this.getBeanClass(), mainInterface, localInterfaces);
        return this.getBusinessLocalHome(interfaces, mainInterface);
    }
    
    public BusinessLocalHome getBusinessLocalHome(List<Class> interfaces, Class mainInterface) {
        if (getBusinessLocalInterfaces().size() == 0){
            throw new IllegalStateException("This component has no business local interfaces: " + getDeploymentID());
        }
        if (interfaces.size() == 0){
            throw new IllegalArgumentException("No interface classes were specified");
        }
        for (Class clazz : interfaces) {
            if (!getBusinessLocalInterfaces().contains(clazz)){
                throw new IllegalArgumentException("Not a business interface of this bean:" + clazz.getName());
            }
        }

        return (BusinessLocalHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCAL_HOME, interfaces, mainInterface);
    }

    //unused
    public BusinessRemoteHome getBusinessRemoteHome() {
        return getBusinessRemoteHome(getBusinessRemoteInterfaces(), null);
    }

    public BusinessRemoteHome getBusinessRemoteHome(Class mainInterface) {
        List<Class> remoteInterfaces = this.getBusinessRemoteInterfaces();

        List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(this.getBeanClass(), mainInterface, remoteInterfaces);
        return this.getBusinessRemoteHome(interfaces, mainInterface);
    }
    
    public BusinessRemoteHome getBusinessRemoteHome(List<Class> interfaces, Class mainInterface) {
        if (getBusinessRemoteInterfaces().size() == 0){
            throw new IllegalStateException("This component has no business remote interfaces: " + getDeploymentID());
        }
        if (interfaces.size() == 0){
            throw new IllegalArgumentException("No interface classes were specified");
        }
        for (Class clazz : interfaces) {
            if (!getBusinessRemoteInterfaces().contains(clazz)){
                throw new IllegalArgumentException("Not a business interface of this bean:" + clazz.getName());
            }
        }

        return (BusinessRemoteHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_REMOTE_HOME, interfaces, mainInterface);
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public void setBeanManagedTransaction(boolean value) {
        isBeanManagedTransaction = value;
    }

    public void setBeanManagedConcurrency(boolean beanManagedConcurrency) {
        isBeanManagedConcurrency = beanManagedConcurrency;
    }

    public Context getJndiEnc() {
        return jndiContext;
    }

    public boolean isReentrant() {
        return isReentrant;
    }

    public void setIsReentrant(boolean reentrant) {
        isReentrant = reentrant;
    }

    public Method getMatchingBeanMethod(Method interfaceMethod) {
        Method method = methodMap.get(interfaceMethod);
        return (method == null) ? interfaceMethod : method;
    }

    public MethodContext getMethodContext(Method method) {
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

    public void setMethodConcurrencyAttribute(Method method, LockType concurrencyAttribute) {
        getMethodContext(method).setLockType(concurrencyAttribute);
    }


    /**
     * TODO: Move to MethodContext
     */
    public void setMethodTransactionAttribute(Method method, TransactionType transactionType) throws OpenEJBException {
        setMethodTransactionAttribute(method, transactionType, null);
    }

    /**
     * TODO: Move to MethodContext
     */
    public void setMethodTransactionAttribute(Method method, TransactionType transactionType, String view) throws OpenEJBException {

//        method = getMatchingBeanMethod(method);

        if (view == null) {
            getMethodContext(method).setTransactionType(transactionType);
        } else {
            initViewMethodContext(method, view).setTransactionType(transactionType);
        }

        return;

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
        return removeMethods;
    }

    /**
     * TODO: Move to MethodContext
     */
    private final Map<Method, Boolean> removeExceptionPolicy = new HashMap<Method,Boolean>();

    /**
     * TODO: Move to MethodContext
     */
    public void setRetainIfExeption(Method removeMethod, boolean retain){
        if (getRemoveMethods().contains(removeMethod)){
            removeExceptionPolicy.put(removeMethod, retain);
        }
    }

    /**
     * TODO: Move to MethodContext
     */
    public boolean retainIfExeption(Method removeMethod){
        Boolean retain = removeExceptionPolicy.get(removeMethod);
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

    public void addSystemInterceptor(Object interceptor) {
        systemInterceptors.add(new InterceptorInstance(interceptor));
    }

    public void addUserInterceptor(Object interceptor) {
        userInterceptors.add(new InterceptorInstance(interceptor));
    }

    public List<InterceptorInstance> getUserAndSystemInterceptors() {
        List<InterceptorInstance> interceptors = new ArrayList<InterceptorInstance>(systemInterceptors);
        interceptors.addAll(userInterceptors);
        return interceptors;
    }

    private final Set<InterceptorData> cdiInterceptors = new LinkedHashSet<InterceptorData>();

    public List<InterceptorData> getCallbackInterceptors() {
        List<InterceptorData> datas = getInterceptorData();
        datas.addAll(callbackInterceptors);
        datas.addAll(cdiInterceptors);
        return datas;
    }

    public void setCallbackInterceptors(List<InterceptorData> callbackInterceptors) {
        //TODO shouldn't we remove the old callbackInterceptors from instanceScopedInterceptors before adding the new ones?
        this.callbackInterceptors.clear();
        this.callbackInterceptors.addAll(callbackInterceptors);
        this.instanceScopedInterceptors.addAll(callbackInterceptors);
    }

    public List<InterceptorData> getCdiInterceptors() {
        return new ArrayList<InterceptorData>(cdiInterceptors);
    }

    public void setCdiInterceptors(List<InterceptorData> cdiInterceptors) {
        this.cdiInterceptors.clear();
        this.cdiInterceptors.addAll(cdiInterceptors);
        this.instanceScopedInterceptors.addAll(cdiInterceptors);
    }

    public List<InterceptorData> getMethodInterceptors(Method method) {
        return getMethodContext(method).getInterceptors();
    }

    public List<InterceptorData> getInterceptorData() {
        List<InterceptorData> datas = new ArrayList<InterceptorData>();
        for (InterceptorInstance instance : getUserAndSystemInterceptors()) {
            datas.add(instance.getData());
        }
        return datas;
    }

    public void addCdiMethodInterceptor(final Method method, final InterceptorData interceptor) {
        getMethodContext(method).addCdiInterceptor(interceptor);
        instanceScopedInterceptors.add(interceptor);
    }

    public void setMethodInterceptors(Method method, List<InterceptorData> interceptors) {
        getMethodContext(method).setInterceptors(interceptors);
        this.instanceScopedInterceptors.addAll(interceptors);
    }

    public void createMethodMap() throws org.apache.openejb.SystemException {
        if (remoteInterface != null) {
            mapObjectInterface(remoteInterface);
            mapHomeInterface(homeInterface);
        }

        if (localInterface != null) {
            mapObjectInterface(localInterface);
        }
        if (localHomeInterface != null) {
            mapHomeInterface(localHomeInterface);
        }

        if (serviceEndpointInterface != null) {
            mapObjectInterface(serviceEndpointInterface);
        }

        for (Class businessLocal : businessLocals) {
            mapObjectInterface(businessLocal);
        }

        for (Class businessRemote : businessRemotes) {
            mapObjectInterface(businessRemote);
        }

        if (componentType == BeanType.MESSAGE_DRIVEN && MessageDrivenBean.class.isAssignableFrom(beanClass)) {
            try {
                createMethod = beanClass.getMethod("ejbCreate");
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
                    for (Method method : getRemoveMethods()) {
                        if (method.getParameterTypes().length == 0){
                            beanMethod = method;
                            break;
                        }
                    }
                    if (beanMethod == null && (homeInterface != null || localHomeInterface != null)){
                        throw new IllegalStateException("Bean class has no @Remove methods to match EJBObject.remove() or EJBLocalObject.remove().  A no-arg remove method must be added: beanClass="+beanClass.getName());
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
                Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove");
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

        if (mdbInterface != null) {
            mapObjectInterface(mdbInterface);
        }
    }

    private void mapHomeInterface(Class intrface) {
        Method [] homeMethods = intrface.getMethods();
        for (Method method : homeMethods) {
            Class owner = method.getDeclaringClass();
            if (owner == EJBHome.class || owner == EJBLocalHome.class) {
                continue;
            }

            try {
                Method beanMethod = null;
                if (method.getName().startsWith("create")) {
                    StringBuilder ejbCreateName = new StringBuilder(method.getName());
                    ejbCreateName.replace(0, 1, "ejbC");
                    beanMethod = beanClass.getMethod(ejbCreateName.toString(), method.getParameterTypes());
                    createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching
                    parameters. This code maps that relationship.
                    */
                    if (this.componentType == BeanType.BMP_ENTITY || this.componentType == BeanType.CMP_ENTITY) {
                        ejbCreateName.insert(3, "Post");
                        Class clazz = beanClass;
                        if (cmpImplClass != null) clazz = cmpImplClass;
                        Method postCreateMethod = clazz.getMethod(ejbCreateName.toString(), method.getParameterTypes());
                        postCreateMethodMap.put(createMethod, postCreateMethod);
                    }
                    /*
                     * Stateless session beans only have one create method. The getCreateMethod is
                     * used by instance manager of the core.stateless.StatelessContainer as a convenience
                     * method for obtaining the ejbCreate method.
                    */
                } else if (method.getName().startsWith("find")) {
                    if (this.componentType == BeanType.BMP_ENTITY) {

                        String beanMethodName = "ejbF" + method.getName().substring(1);
                        beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                    }
                } else {
                    String beanMethodName = "ejbHome" + method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1);
                    beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                }
                if (beanMethod != null) {
                    mapMethods(method, beanMethod);
                }
            } catch (NoSuchMethodException nsme) {
//                throw new RuntimeException("Invalid method [" + method + "] Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    public void mapMethods(Method interfaceMethod, Method beanMethod){
        methodMap.put(interfaceMethod, beanMethod);
    }

    private void mapObjectInterface(Class intrface) {
        if (intrface == BusinessLocalHome.class || intrface == BusinessRemoteHome.class || intrface == ServiceEndpoint.class){
            return;
        }

        Method [] interfaceMethods = intrface.getMethods();
        for (Method method : interfaceMethods) {
            Class declaringClass = method.getDeclaringClass();
            if (declaringClass == EJBObject.class || declaringClass == EJBLocalObject.class) {
                continue;
            }
            try {
                Method beanMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());
                mapMethods(method, beanMethod);
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException("Invalid method [" + method + "]. Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    public List<Class> getObjectInterface(Class homeInterface){
        if (BusinessLocalHome.class.isAssignableFrom(homeInterface)){
            return getBusinessLocalInterfaces();
        } else if (BusinessRemoteHome.class.isAssignableFrom(homeInterface)){
            return getBusinessRemoteInterfaces();
        } else if (EJBLocalHome.class.isAssignableFrom(homeInterface)){
            List<Class> classes = new ArrayList<Class>();
            classes.add(getLocalInterface());
            return classes;
        } else if (EJBHome.class.isAssignableFrom(homeInterface)){
            List<Class> classes = new ArrayList<Class>();
            classes.add(getRemoteInterface());
            return classes;
        } else {
            throw new IllegalArgumentException("Cannot determine object interface for "+homeInterface);
        }
    }

    public Method getCreateMethod() {
        return createMethod;
    }

    public Method getMatchingPostCreateMethod(Method createMethod) {
        return this.postCreateMethodMap.get(createMethod);
    }

    public boolean isAsynchronous(Method method) {
        Method matchingBeanMethod = getMatchingBeanMethod(method);
        Class<?> returnType = matchingBeanMethod.getReturnType();
        if (returnType != void.class && returnType != Future.class) {
            return false;
        }
        if (asynchronousClasses.contains(matchingBeanMethod.getDeclaringClass())) {
            return true;
        }
        MethodContext methodContext = methodContextMap.get(matchingBeanMethod);
        return methodContext == null ? false : methodContext.isAsynchronous();
    }

    //
    // CMP specific data
    //

    private boolean cmp2;
    private KeyGenerator keyGenerator;
    private String primaryKeyField;
    private Class cmpImplClass;
    private String abstractSchemaName;

    private Set<String> remoteQueryResults = new TreeSet<String>();

    public boolean isCmp2() {
        return cmp2;
    }

    public void setCmp2(boolean cmp2) {
        this.cmp2 = cmp2;
    }

    public String getPrimaryKeyField() {
        return primaryKeyField;
    }

    public void setPrimaryKeyField(String primaryKeyField) {
        this.primaryKeyField = primaryKeyField;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void setRemoteQueryResults(String methodSignature) {
        remoteQueryResults.add(methodSignature);
    }

    public boolean isRemoteQueryResults(String methodSignature) {
        return remoteQueryResults.contains(methodSignature);
    }

    public Class getCmpImplClass() {
        return cmpImplClass;
    }

    public void setCmpImplClass(Class cmpImplClass) {
        this.cmpImplClass = cmpImplClass;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public void setAbstractSchemaName(String abstractSchemaName) {
        this.abstractSchemaName = abstractSchemaName;
    }

    public Method getEjbTimeout() {
        return ejbTimeout;
    }

    public void setEjbTimeout(Method ejbTimeout) {
        this.ejbTimeout = ejbTimeout;
    }

    public EjbTimerService getEjbTimerService() {
        return ejbTimerService;
    }

    public void setEjbTimerService(EjbTimerService ejbTimerService) {
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

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public String toString() {
        return "BeanContext(id="+getDeploymentID()+")";
    }

    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
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

    public Duration getAccessTimeout(Method beanMethod) {
        return getMethodContext(beanMethod).getAccessTimeout();
    }
    
	public Duration getAccessTimeout() {
		return accessTimeout;
	}

	public void setAccessTimeout(Duration accessTimeout) {
		this.accessTimeout = accessTimeout;
	}

	public Duration getStatefulTimeout() {
		return statefulTimeout;
	}

	public void setStatefulTimeout(Duration statefulTimeout) {
		this.statefulTimeout = statefulTimeout;
	}


    public Class<Object> getManagedClass() {
        if (isDynamicallyImplemented()) {
            return (Class<Object>) getProxyClass();
        }
        return beanClass;
    }

    public InstanceContext newInstance() throws Exception {
        ThreadContext callContext = new ThreadContext(this, null, Operation.INJECTION);
        ThreadContext oldContext = ThreadContext.enter(callContext);

        WebBeansContext webBeansContext = null;
        AbstractInjectionTargetBean<Object> beanDefinition = null;

        webBeansContext = getModuleContext().getAppContext().getWebBeansContext();
        beanDefinition = get(CdiEjbBean.class);

        if (isDynamicallyImplemented()) {
            if (!InvocationHandler.class.isAssignableFrom(getProxyClass())) {
                throw new OpenEJBException("proxy class can only be InvocationHandler");
            }
        }

        ConstructorInjectionBean<Object> beanConstructor = new ConstructorInjectionBean<Object>(webBeansContext, getManagedClass());
        if (beanDefinition == null) {
            beanDefinition = beanConstructor;
        }

        try {
            final Context ctx = this.getJndiEnc();
            final Class beanClass = this.getBeanClass();

            CurrentCreationalContext<Object> currentCreationalContext = get(CurrentCreationalContext.class);
            CreationalContext<Object> creationalContext = (currentCreationalContext != null) ? currentCreationalContext.get() : null;

            if (creationalContext == null) {
                creationalContext = webBeansContext.getBeanManagerImpl().createCreationalContext(beanDefinition);
            }

            // Create bean instance
            final Object beanInstance;
            final InjectionProcessor injectionProcessor = new InjectionProcessor(beanConstructor.create(creationalContext), this.getInjections(), InjectionProcessor.unwrap(ctx));
            if (!isDynamicallyImplemented()) {
                beanInstance = injectionProcessor.createInstance();
                inject(beanInstance, creationalContext);
            } else {
                InvocationHandler handler = (InvocationHandler) injectionProcessor.createInstance();
                beanInstance = DynamicProxyImplFactory.newProxy(this, handler);
                inject(handler, creationalContext);
            }

            // Create interceptors
            final HashMap<String, Object> interceptorInstances = new HashMap<String, Object>();

            // Add the stats interceptor instance and other already created interceptor instances
            for (InterceptorInstance interceptorInstance : this.getUserAndSystemInterceptors()) {
                final Class clazz = interceptorInstance.getData().getInterceptorClass();
                interceptorInstances.put(clazz.getName(), interceptorInstance.getInterceptor());
            }

            for (InterceptorData interceptorData : this.getInstanceScopedInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) {
                    continue;
                }

                final Class clazz = interceptorData.getInterceptorClass();

                final ConstructorInjectionBean interceptorConstructor = new ConstructorInjectionBean(webBeansContext, clazz);
                final InjectionProcessor interceptorInjector = new InjectionProcessor(interceptorConstructor.create(creationalContext), this.getInjections(), org.apache.openejb.InjectionProcessor.unwrap(ctx));
                try {
                    final Object interceptorInstance = interceptorInjector.createInstance();

                    // TODO we likely don't want to create a new one each time -- investigate the destroy() method
                    try {
                        OWBInjector interceptorCdiInjector = new OWBInjector(webBeansContext);
                        interceptorCdiInjector.inject(interceptorInstance, creationalContext);
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
                Set<Method> callbacks = callbackInterceptors.get(callbackInterceptors.size() -1).getPostConstruct();
                if (callbacks.isEmpty()) {
                    transactionType = TransactionType.RequiresNew;
                } else {
                    transactionType = getTransactionType(callbacks.iterator().next());
                    if (transactionType == TransactionType.Required) {
                        transactionType = TransactionType.RequiresNew;
                    }
                }
            } else {
                transactionType = isBeanManagedTransaction()? TransactionType.BeanManaged: TransactionType.NotSupported;
            }
            TransactionPolicy transactionPolicy = EjbTransactionUtil.createTransactionPolicy(transactionType, callContext);
            try{
                //Call the chain
                postConstruct.invoke();                
            } catch(Throwable e) {
                //RollBack Transaction
                EjbTransactionUtil.handleSystemException(transactionPolicy, e, callContext);
            }
            finally{
                EjbTransactionUtil.afterInvoke(transactionPolicy, callContext);
            }

            return new InstanceContext(this, beanInstance, interceptorInstances, creationalContext);
        } finally {
            ThreadContext.exit(oldContext);
        }                        
    }

    protected <X> X getBean(Class<X> clazz, Bean<?> bean) {
        return clazz.cast(bean);
    }

    public <T> void inject(T instance, CreationalContext<T> ctx) {

        WebBeansContext webBeansContext = getModuleContext().getAppContext().getWebBeansContext();

        AbstractInjectionTargetBean<Object> beanDefinition = get(CdiEjbBean.class);

        final ConstructorInjectionBean<Object> beanConstructor = new ConstructorInjectionBean<Object>(webBeansContext, getManagedClass());

        if (beanDefinition == null) {
            beanDefinition = beanConstructor;
        }

        if (!(ctx instanceof CreationalContextImpl)) {
            ctx = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(ctx, beanDefinition);
        }

        Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();
        boolean isInjectionToAnotherBean = false;
        try {
            Contextual<?> contextual = null;
            if (ctx instanceof CreationalContextImpl) {
                contextual = ((CreationalContextImpl) ctx).getBean();
                isInjectionToAnotherBean = contextual == getBean(InjectionTargetBean.class, beanDefinition) ? false : true;
            }

            if (!isInjectionToAnotherBean) {
                AbstractInjectable.instanceUnderInjection.set(instance);
            }

            InjectionTargetBean<T> bean = getBean(InjectionTargetBean.class, beanDefinition);

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

    public void createAsynchronousMethodSet(){
        for(Map.Entry<Method, MethodContext> entry : methodContextMap.entrySet()) {
            if(entry.getValue().isAsynchronous()) {
                asynchronousMethodSignatures.add(generateMethodSignature(entry.getKey()));
            }
        }
        for(Class<?> cls : asynchronousClasses) {
            for(Method method : cls.getDeclaredMethods()) {
                if(Modifier.isPublic(method.getModifiers())) {
                    asynchronousMethodSignatures.add(generateMethodSignature(method));
                }
            }
        }
    }

    private String generateMethodSignature(Method method) {
        StringBuilder buffer = new StringBuilder(method.getName());
        for(Class<?> parameterType : method.getParameterTypes()) {
            buffer.append(parameterType.getName());
        }
        return buffer.toString();
    }

    private MethodContext getViewMethodContext(Method method, String view) {
        ViewContext viewContext = this.viewContextMap.get(view);
        return (viewContext == null) ? null : viewContext.getMethodContext(method);
    }

    private MethodContext initViewMethodContext(Method method, String view) {
        ViewContext viewContext = this.viewContextMap.get(view);
        if (viewContext == null) {
            viewContext = new ViewContext();
            viewContextMap.put(view, viewContext);
        }

        return viewContext.initMethodContext(method);
    }

    public Class<?> getProxyClass() {
        if (isDynamicallyImplemented() && proxyClass == null) {
            return QueryProxy.class;
        }
        return proxyClass;
    }

    public class ViewContext {

        private final Map<Method, MethodContext> methodContextMap = new HashMap<Method, MethodContext>();

        public MethodContext getMethodContext(Method method) {
            return methodContextMap.get(method);
        }

        public MethodContext initMethodContext(Method method) {
            MethodContext methodContext = methodContextMap.get(method);
            if (methodContext != null) return methodContext;

            methodContext = new MethodContext(BeanContext.this, method);
            methodContextMap.put(method, methodContext);

            return methodContext;
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
