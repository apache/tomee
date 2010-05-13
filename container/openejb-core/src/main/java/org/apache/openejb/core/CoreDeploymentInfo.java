/**
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
package org.apache.openejb.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Properties;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenBean;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.SessionSynchronization;
import javax.naming.Context;
import javax.persistence.EntityManagerFactory;

import org.apache.openejb.BeanType;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.Injection;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.timer.MethodSchedule;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class CoreDeploymentInfo implements org.apache.openejb.DeploymentInfo {

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

    private final List<Method> aroundInvoke = new ArrayList<Method>();

    private final List<Method> postConstruct = new ArrayList<Method>();
    private final List<Method> preDestroy = new ArrayList<Method>();

    private final List<Method> postActivate = new ArrayList<Method>();
    private final List<Method> prePassivate = new ArrayList<Method>();

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
    private final Map<Class, Object> data = new HashMap<Class, Object>();

    private final Properties properties = new Properties();

    private String ejbName;
    private String moduleId;
    private String runAs;

    private Object containerData;

    private final DeploymentContext context;

    private Method createMethod = null;

    private final Map<Method, Method> postCreateMethodMap = new HashMap<Method, Method>();
    private final BeanType componentType;

    private final Map<Method, Collection<String>> methodPermissions = new HashMap<Method, Collection<String>>();
    private final Map<Method, Byte> methodConcurrencyAttributes = new HashMap<Method, Byte>();

    private final Map<Method, TransactionType> methodTransactionType = new HashMap<Method, TransactionType>();
    private TransactionPolicyFactory transactionPolicyFactory;

    private final List<MethodSchedule> methodSchedules = new ArrayList<MethodSchedule>();
    private final Map<Method, List<InterceptorData>> methodInterceptors = new HashMap<Method, List<InterceptorData>>();
    private final List<InterceptorData> callbackInterceptors = new ArrayList<InterceptorData>();
    private final Set<InterceptorData> instanceScopedInterceptors = new HashSet<InterceptorData>();

    private final List<InterceptorInstance> systemInterceptors = new ArrayList<InterceptorInstance>(); 
    private final Map<Method, Method> methodMap = new HashMap<Method, Method>();
    private final Map<String, String> securityRoleReferenceMap = new HashMap<String, String>();
    private String jarPath;
    private final Map<String, String> activationProperties = new HashMap<String, String>();
    private final List<Injection> injections = new ArrayList<Injection>();
    private Index<EntityManagerFactory,Map> extendedEntityManagerFactories;
    private final Map<Class, InterfaceType> interfaces = new HashMap<Class, InterfaceType>();
    private final Map<Class, ExceptionType> exceptions = new HashMap<Class, ExceptionType>();
    private boolean loadOnStartup;
    private boolean localbean;

    public Class getInterface(InterfaceType interfaceType) {
        switch(interfaceType){
            case EJB_HOME: return getHomeInterface();
            case EJB_OBJECT: return getRemoteInterface();
            case EJB_LOCAL_HOME: return getLocalHomeInterface();
            case EJB_LOCAL: return getLocalInterface();
            case BUSINESS_LOCAL: return getBusinessLocalInterface();
            case BUSINESS_REMOTE: return getBusinessRemoteInterface();
            case BUSINESS_REMOTE_HOME: return DeploymentInfo.BusinessRemoteHome.class;
            case BUSINESS_LOCAL_HOME: return DeploymentInfo.BusinessLocalHome.class;
            case SERVICE_ENDPOINT: return getServiceEndpointInterface();
            case LOCALBEAN: return getBeanClass();
            case BUSINESS_LOCALBEAN_HOME: return DeploymentInfo.BusinessLocalBeanHome.class;
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

    public CoreDeploymentInfo(DeploymentContext context,
                              Class beanClass, Class homeInterface,
                              Class remoteInterface,
                              Class localHomeInterface,
                              Class localInterface,
                              Class serviceEndpointInterface, List<Class> businessLocals, List<Class> businessRemotes, Class pkClass,
                              BeanType componentType
    ) throws SystemException {
        if (context == null || beanClass == null) {
            throw new NullPointerException("context or beanClass input parameter is null");
        }
        this.context = context;
        this.pkClass = pkClass;

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

        this.componentType = componentType;

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

        addInterface(DeploymentInfo.BusinessRemoteHome.class, InterfaceType.BUSINESS_REMOTE_HOME);
        for (Class businessRemote : this.businessRemotes) {
            addInterface(businessRemote, InterfaceType.BUSINESS_REMOTE);
        }

        addInterface(DeploymentInfo.BusinessLocalHome.class, InterfaceType.BUSINESS_LOCAL_HOME);
        for (Class businessLocal : this.businessLocals) {
            addInterface(businessLocal, InterfaceType.BUSINESS_LOCAL);
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

    public void addApplicationException(Class exception, boolean rollback) {
        if (rollback) {
            exceptions.put(exception, ExceptionType.APPLICATION_ROLLBACK);
        } else {
            exceptions.put(exception, ExceptionType.APPLICATION);
        }
    }

    public ExceptionType getExceptionType(Throwable e) {
        // Errors are always system exceptions
        if (!(e instanceof Exception)) {
            return ExceptionType.SYSTEM;
        }

        // check the registered app exceptions
        ExceptionType type = exceptions.get(e.getClass());
        if (type != null) {
            return type;
        }

        // Unregistered - runtime exceptions are system exception and the rest are application exceptions
        if (e instanceof RuntimeException) {
            return ExceptionType.SYSTEM;
        } else {
            return ExceptionType.APPLICATION;
        }
    }

    public CoreDeploymentInfo(DeploymentContext context, Class beanClass, Class mdbInterface, Map<String, String> activationProperties) throws SystemException {
        this.context = context;
        this.beanClass = beanClass;
        this.mdbInterface = mdbInterface;
        this.activationProperties.putAll(activationProperties);
        this.componentType = BeanType.MESSAGE_DRIVEN;

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout", Timer.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
        createMethodMap();
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T set(Class<T> type, T value) {
        return (T) data.put(type, value);
    }

    public Properties getProperties() {
        return properties;
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

    public Object getContainerData() {
        return containerData;
    }

    public void setContainerData(Object containerData) {
        this.containerData = containerData;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public BeanType getComponentType() {
        return componentType;
    }

    public byte getConcurrencyAttribute(Method method) {
        Byte byteWrapper = methodConcurrencyAttributes.get(method);

        if (byteWrapper == null){
            Method beanMethod = getMatchingBeanMethod(method);
            byteWrapper = methodConcurrencyAttributes.get(beanMethod);
        }

        if (byteWrapper == null) {
            return WRITE_LOCK;
        } else {
            return byteWrapper;
        }
    }

    public TransactionType getTransactionType(Method method) {
        // Check the cache
        TransactionType type = methodTransactionType.get(method);
        if (type != null) {
            return type;
        }

        // Bean managed EJBs always get the BeanManaged policy
        if (isBeanManagedTransaction) {
            return TransactionType.BeanManaged;
        }

        // Check the matching bean method for the supplied method
        Method beanMethod = getMatchingBeanMethod(method);
        if (beanMethod != null){
            type = methodTransactionType.get(beanMethod);
            if (type != null) {
                return type;
            }
        }

        // All transaction attributes should have been set during deployment, so log a message
        Logger log = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
        log.debug("The following method doesn't have a transaction policy assigned: " + method);

        // default transaction policy is required
        type = TransactionType.Required;

        // cache this default to avoid extra log messages
        methodTransactionType.put(method, type);
        return type;
    }

    public TransactionPolicyFactory getTransactionPolicyFactory() {
        return transactionPolicyFactory;
    }

    public void setTransactionPolicyFactory(TransactionPolicyFactory transactionPolicyFactory) {
        this.transactionPolicyFactory = transactionPolicyFactory;
    }

    public Collection<String> getAuthorizedRoles(Method method) {
        Collection<String> roleSet = methodPermissions.get(method);
        if (roleSet == null) {
            return Collections.emptySet();
        }
        return roleSet;
    }

    public String [] getAuthorizedRoles(String action) {
        return null;
    }

    public Container getContainer() {
        return container;
    }

    public Object getDeploymentID() {
        return context.getId();
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

    public void setActivationProperties(Map<String, String> activationProperties) {
        this.activationProperties.clear();
        this.activationProperties.putAll(activationProperties);
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

    public BusinessLocalHome getBusinessLocalHome() {
        return getBusinessLocalHome(getBusinessLocalInterfaces());
    }

    public BusinessLocalBeanHome getBusinessLocalBeanHome() {
        List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(this.beanClass);
        return (BusinessLocalBeanHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCALBEAN_HOME, interfaces);
    }

    public BusinessLocalHome getBusinessLocalHome(List<Class> interfaces) {
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

        return (BusinessLocalHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_LOCAL_HOME, interfaces);
    }

    public BusinessRemoteHome getBusinessRemoteHome() {
        return getBusinessRemoteHome(getBusinessRemoteInterfaces());
    }

    public BusinessRemoteHome getBusinessRemoteHome(List<Class> interfaces) {
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

        return (BusinessRemoteHome) EjbHomeProxyHandler.createHomeProxy(this, InterfaceType.BUSINESS_REMOTE_HOME, interfaces);
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
        return context.getJndiContext();
    }

    public ClassLoader getClassLoader() {
        return context.getClassLoader();
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

    public void appendMethodPermissions(Method m, List<String> roleNames) {
        Collection<String> hs = methodPermissions.get(m);
        if (hs == null) {
            hs = new HashSet<String>();// FIXME: Set appropriate load and intial capacity
            methodPermissions.put(m, hs);
        }
        for (String roleName : roleNames) {
            hs.add(roleName);
        }
    }

    public String getSecurityRole(String securityRoleReference) {
        return securityRoleReferenceMap.get(securityRoleReference);
    }

    public void addSecurityRoleReference(String securityRoleReference, String linkedRoleName) {
        securityRoleReferenceMap.put(securityRoleReference, linkedRoleName);
    }

    public void setMethodConcurrencyAttribute(Method method, String concurrencyAttribute) {
        if ("Read".equals(concurrencyAttribute)){
            this.methodConcurrencyAttributes.put(method, READ_LOCK);
        } else if ("Write".equals(concurrencyAttribute)){
            this.methodConcurrencyAttributes.put(method, WRITE_LOCK);
        } else {
            throw new IllegalArgumentException("Unsupported MethodConcurrencyAttribute '"+concurrencyAttribute+"'");
        }
    }


    public void setMethodTransactionAttribute(Method method, String transAttribute) throws OpenEJBException {
        TransactionType transactionType;
        if (transAttribute.equalsIgnoreCase("Supports")) {
            transactionType = TransactionType.Supports;

        } else if (transAttribute.equalsIgnoreCase("RequiresNew")) {
            transactionType = TransactionType.RequiresNew;

        } else if (transAttribute.equalsIgnoreCase("Mandatory")) {
            transactionType = TransactionType.Mandatory;

        } else if (transAttribute.equalsIgnoreCase("NotSupported")) {
            transactionType = TransactionType.NotSupported;

        } else if (transAttribute.equalsIgnoreCase("Required")) {
            transactionType = TransactionType.Required;

        } else if (transAttribute.equalsIgnoreCase("Never")) {
            transactionType = TransactionType.Never;

        } else {
            throw new IllegalArgumentException("Invalid transaction attribute \"" + transAttribute + "\" declared for method " + method.getName() + ". Please check your configuration.");
        }

        // Only the NOT_SUPPORTED and REQUIRED transaction attributes may be used for message-driven
        // bean message listener methods. The use of the other transaction attributes is not meaningful
        // for message-driven bean message listener methods because there is no pre-existing client transaction
        // context(REQUIRES_NEW, SUPPORTS) and no client to handle exceptions (MANDATORY, NEVER).
        if (componentType.isMessageDriven() && !isBeanManagedTransaction) {
            if (transactionType != TransactionType.NotSupported && transactionType != TransactionType.Required) {

                if (method.equals(this.ejbTimeout) && transactionType == TransactionType.RequiresNew) {
                    // do nothing. This is allowed as the timer callback method for a message driven bean
                    // can also have a transaction policy of RequiresNew Sec 5.4.12 of Ejb 3.0 Core Spec
                } else {
                    throw new OpenEJBException("The transaction attribute " + transactionType + " is not supported for the method "
                                               + method.getName() + " of the Message Driven Bean " + beanClass.getName());
                }
            }
        }
        methodTransactionType.put(method, transactionType);
    }

    public List<Method> getAroundInvoke() {
        return aroundInvoke;
    }

    public List<Method> getPostConstruct() {
        return postConstruct;
    }

    public List<Method> getPreDestroy() {
        return preDestroy;
    }

    public List<Method> getPostActivate() {
        return postActivate;
    }

    public List<Method> getPrePassivate() {
        return prePassivate;
    }

    public List<Method> getRemoveMethods() {
        return removeMethods;
    }

    private final Map<Method, Boolean> removeExceptionPolicy = new HashMap<Method,Boolean>();

    public void setRetainIfExeption(Method removeMethod, boolean retain){
        if (getRemoveMethods().contains(removeMethod)){
            removeExceptionPolicy.put(removeMethod, retain);
        }
    }

    public boolean retainIfExeption(Method removeMethod){
        Boolean retain = removeExceptionPolicy.get(removeMethod);
        return retain != null && retain;
    }

    public List<MethodSchedule> getMethodSchedules() {
        return methodSchedules;
    }

    public void setMethodSchedules(List<MethodSchedule> schedules) {
        methodSchedules.addAll(schedules);
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

    public List<InterceptorInstance> getSystemInterceptors() {
        return systemInterceptors;
    }

    public List<InterceptorData> getCallbackInterceptors() {
        return callbackInterceptors;
    }

    public void setCallbackInterceptors(List<InterceptorData> callbackInterceptors) {
        this.callbackInterceptors.clear();
        this.callbackInterceptors.addAll(callbackInterceptors);
        this.instanceScopedInterceptors.addAll(callbackInterceptors);
    }

    public List<InterceptorData> getMethodInterceptors(Method method) {

        List<InterceptorData> interceptors = methodInterceptors.get(method);

        if (interceptors == null) interceptors = Collections.EMPTY_LIST;

        if (systemInterceptors.size() <= 0) return interceptors;

        // we have system interceptors to add to the beginning of the stack
        
        List<InterceptorData> datas = new ArrayList<InterceptorData>(systemInterceptors.size() + interceptors.size());

        for (InterceptorInstance instance : systemInterceptors) {
            datas.add(instance.getData());
        }

        datas.addAll(interceptors);
        
        return datas;
    }

    public void setMethodInterceptors(Method method, List<InterceptorData> interceptors) {
        methodInterceptors.put(method, interceptors);
        this.instanceScopedInterceptors.addAll(interceptors);
    }

    public void createMethodMap() throws org.apache.openejb.SystemException {
        if (remoteInterface != null) {
            mapObjectInterface(remoteInterface);
            mapHomeInterface(homeInterface);
        }

        if (localInterface != null) {
            mapObjectInterface(localInterface);
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

    protected String extractHomeBeanMethodName(String methodName) {
        if (methodName.equals("create")) {
            return "ejbCreate";
        } else if (methodName.startsWith("find")) {
            return "ejbF" + methodName.substring(1);
        } else {
            return "ejbH" + methodName.substring(1);
        }
    }

    public Method getCreateMethod() {
        return createMethod;
    }

    public Method getMatchingPostCreateMethod(Method createMethod) {
        return this.postCreateMethodMap.get(createMethod);
    }

    //
    // CMP specific data
    //

    private boolean cmp2;
    private KeyGenerator keyGenerator;
    private String primaryKeyField;
    private String[] cmrFields;
    private Class cmpImplClass;
    private String abstractSchemaName;

    private Map<Method, String> queryMethodMap = new HashMap<Method, String>();
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

    public String [] getCmrFields() {
        return cmrFields;
    }

    public void setCmrFields(String [] cmrFields) {
        this.cmrFields = cmrFields;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void addQuery(Method queryMethod, String queryString) {
        queryMethodMap.put(queryMethod, queryString);
    }

    public String getQuery(Method queryMethod) {
        return queryMethodMap.get(queryMethod);
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

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
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
        return moduleId;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public String toString() {
        return "DeploymentInfo(id="+getDeploymentID()+")";
    }

    public void setServiceEndpointInterface(Class serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
        mapObjectInterface(serviceEndpointInterface);
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
        return !isBeanManagedTransaction() && SessionSynchronization.class.isAssignableFrom(beanClass);
    }

    public boolean isLocalbean() {
        return localbean;
    }

    public void setLocalbean(boolean localbean) {
        this.localbean = localbean;
    }

    public Class getBusinessLocalBeanInterface() {
        if (this.isLocalbean()) {
            return this.beanClass;
        }

        return null;
    }
}
