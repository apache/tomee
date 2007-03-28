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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.SessionSynchronization;
import javax.ejb.MessageDrivenBean;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.persistence.EntityManagerFactory;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.apache.openejb.Container;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.SystemException;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.BeanType;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.stateful.SessionSynchronizationTxPolicy;
import org.apache.openejb.core.stateful.StatefulBeanManagedTxPolicy;
import org.apache.openejb.core.stateful.StatefulContainerManagedTxPolicy;
import org.apache.openejb.core.stateful.StatefulEjbHomeHandler;
import org.apache.openejb.core.stateless.StatelessBeanManagedTxPolicy;
import org.apache.openejb.core.stateless.StatelessEjbHomeHandler;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TxManditory;
import org.apache.openejb.core.transaction.TxNever;
import org.apache.openejb.core.transaction.TxNotSupported;
import org.apache.openejb.core.transaction.TxRequired;
import org.apache.openejb.core.transaction.TxRequiresNew;
import org.apache.openejb.core.transaction.TxSupports;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.mdb.MessageDrivenBeanManagedTxPolicy;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.Logger;

/**
 * @org.apache.xbean.XBean element="deployment"
 */
public class CoreDeploymentInfo implements org.apache.openejb.DeploymentInfo {

    private boolean destroyed;
    private Class homeInterface;
    private Class remoteInterface;
    private Class localHomeInterface;
    private Class localInterface;
    private Class beanClass;
    private Class pkClass;
    private Class businessLocal;
    private Class businessRemote;
    private Class mdbInterface;
    private Class serviceEndpointInterface;

    private final List<Method> aroundInvoke = new ArrayList<Method>();

    private final List<Method> postConstruct = new ArrayList<Method>();
    private final List<Method> preDestroy = new ArrayList<Method>();

    private final List<Method> postActivate = new ArrayList<Method>();
    private final List<Method> prePassivate = new ArrayList<Method>();

    private final List<Method> removeMethods = new ArrayList<Method>();

    private Method ejbTimeout;
    private EjbTimerService ejbTimerService;

    private boolean isBeanManagedTransaction;
    private boolean isReentrant;
    private Container container;
    private EJBHome ejbHomeRef;
    private EJBLocalHome ejbLocalHomeRef;
    private BusinessLocalHome businessLocalHomeRef;
    private BusinessRemoteHome businessRemoteHomeRef;
    private final Map<Class, Object> data = new HashMap<Class, Object>();

    private String ejbName;
    private String moduleId;
    private String runAs;

    private Object containerData;

    private final DeploymentContext context;

    private Method createMethod = null;

    private final Map<Method, Method> postCreateMethodMap = new HashMap<Method, Method>();
    private final BeanType componentType;

    private final Map<Method, Collection<String>> methodPermissions = new HashMap<Method, Collection<String>>();
    private final Map<Method, Byte> methodTransactionAttributes = new HashMap<Method, Byte>();
    private final Map<Method, TransactionPolicy> methodTransactionPolicies = new HashMap<Method, TransactionPolicy>();
    private final Map<Method, List<InterceptorData>> methodInterceptors = new HashMap<Method, List<InterceptorData>>();
    private final List<InterceptorData> callbackInterceptors = new ArrayList<InterceptorData>();
    private final Map<Method, Method> methodMap = new HashMap<Method, Method>();
    private final Map<String, String> securityRoleReferenceMap = new HashMap<String, String>();
    private String jarPath;
    private final Map<String, String> activationProperties = new HashMap<String, String>();
    private final List<Injection> injections = new ArrayList<Injection>();
    private Index<EntityManagerFactory,Map> extendedEntityManagerFactories;
    private final Map<Class, InterfaceType> interfaces = new HashMap();

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
            default: throw new IllegalStateException("Unexpected enum constant: " + interfaceType);
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
                              Class businessLocal, Class businessRemote, Class pkClass,
                              BeanType componentType
    ) throws SystemException {

        this.context = context;
        this.pkClass = pkClass;

        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localInterface = localInterface;
        this.localHomeInterface = localHomeInterface;
        this.businessLocal = businessLocal;
        this.businessRemote = businessRemote;
        this.remoteInterface = remoteInterface;
        this.beanClass = beanClass;
        this.pkClass = pkClass;

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

        interfaces.put(javax.ejb.EJBHome.class, InterfaceType.EJB_HOME);
        interfaces.put(javax.ejb.EJBObject.class, InterfaceType.EJB_OBJECT);
        interfaces.put(javax.ejb.EJBLocalHome.class, InterfaceType.EJB_LOCAL_HOME);
        interfaces.put(javax.ejb.EJBLocalObject.class, InterfaceType.EJB_LOCAL);
        interfaces.put(getHomeInterface(), InterfaceType.EJB_HOME);
        interfaces.put(getRemoteInterface(), InterfaceType.EJB_OBJECT);
        interfaces.put(getLocalHomeInterface(), InterfaceType.EJB_LOCAL_HOME);
        interfaces.put(getLocalInterface(), InterfaceType.EJB_LOCAL);
        interfaces.put(getBusinessLocalInterface(), InterfaceType.BUSINESS_LOCAL);
        interfaces.put(getBusinessRemoteInterface(), InterfaceType.BUSINESS_REMOTE);
        interfaces.put(DeploymentInfo.BusinessRemoteHome.class, InterfaceType.BUSINESS_REMOTE_HOME);
        interfaces.put(DeploymentInfo.BusinessLocalHome.class, InterfaceType.BUSINESS_LOCAL_HOME);
        interfaces.put(getServiceEndpointInterface(), InterfaceType.SERVICE_ENDPOINT);
    }

    public CoreDeploymentInfo(DeploymentContext context, Class beanClass, Class mdbInterface, Map<String, String> activationProperties) throws SystemException {
        this.context = context;
        this.beanClass = beanClass;
        this.mdbInterface = mdbInterface;
        this.activationProperties.putAll(activationProperties);
        this.componentType = BeanType.MESSAGE_DRIVEN;

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            try {
                this.ejbTimeout = beanClass.getMethod("ejbTimeout");
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

    public byte getTransactionAttribute(Method method) {
        Byte byteWrapper = methodTransactionAttributes.get(method);
        if (byteWrapper == null) {
            return TX_NOT_SUPPORTED;// non remote or home interface method
        } else {
            return byteWrapper;
        }
    }

    public TransactionPolicy getTransactionPolicy(Method method) {
        TransactionPolicy policy = methodTransactionPolicies.get(method);
        if (policy == null && !isBeanManagedTransaction) {
            Logger log = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
            log.info("The following method doesn't have a transaction policy assigned: " + method);
        }
        if (policy == null && container instanceof TransactionContainer) {
            if (isBeanManagedTransaction) {
                if (componentType == BeanType.STATEFUL) {
                    policy = new StatefulBeanManagedTxPolicy((TransactionContainer) container);
                } else if (componentType == BeanType.STATELESS) {
                    policy = new StatelessBeanManagedTxPolicy((TransactionContainer) container);
                } else if (componentType == BeanType.MESSAGE_DRIVEN) {
                    policy = new MessageDrivenBeanManagedTxPolicy((TransactionContainer) container);
                }
            } else if (componentType == BeanType.STATEFUL) {
                policy = new TxRequired((TransactionContainer) container);
                policy = new StatefulContainerManagedTxPolicy(policy);
            } else if (componentType == BeanType.CMP_ENTITY) {
                // default cmp policy is required
                policy = new TxRequired((TransactionContainer) container);
            } else {
                policy = new TxRequired((TransactionContainer) container);
            }
            methodTransactionPolicies.put(method, policy);
        }
        if (policy == null) {
            policy = new TxSupports((TransactionContainer) container);
        }
        return policy ;
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
        return businessLocal;
    }

    public Class getBusinessRemoteInterface() {
        return businessRemote;
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
            ejbHomeRef = createEJBHomeRef();
        }
        return ejbHomeRef;
    }

    public EJBLocalHome getEJBLocalHome() {
        if (getLocalHomeInterface() == null) {
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (ejbLocalHomeRef == null) {
            ejbLocalHomeRef = createEJBLocalHomeRef();
        }
        return ejbLocalHomeRef;
    }

    public BusinessLocalHome getBusinessLocalHome() {
        if (getBusinessLocalInterface() == null){
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (businessLocalHomeRef == null) {
            businessLocalHomeRef = createBusinessLocalHomeRef();
        }
        return businessLocalHomeRef;
    }

    public BusinessRemoteHome getBusinessRemoteHome() {
        if (getBusinessRemoteInterface() == null){
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (businessRemoteHomeRef == null) {
            businessRemoteHomeRef = createBusinessRemoteHomeRef();
        }
        return businessRemoteHomeRef;
    }

    public void setBeanManagedTransaction(boolean value) {
        isBeanManagedTransaction = value;
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

    public void setMethodTransactionAttribute(Method method, String transAttribute) throws OpenEJBException {
        Byte byteValue = null;
        TransactionPolicy policy = null;

        if (transAttribute.equalsIgnoreCase("Supports")) {
            if (container instanceof TransactionContainer) {
                policy = new TxSupports((TransactionContainer) container);
            }
            byteValue = new Byte(TX_SUPPORTS);

        } else if (transAttribute.equalsIgnoreCase("RequiresNew")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequiresNew((TransactionContainer) container);
            }
            byteValue = new Byte(TX_REQUIRES_NEW);

        } else if (transAttribute.equalsIgnoreCase("Mandatory")) {
            if (container instanceof TransactionContainer) {
                policy = new TxManditory((TransactionContainer) container);
            }
            byteValue = new Byte(TX_MANDITORY);

        } else if (transAttribute.equalsIgnoreCase("NotSupported")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNotSupported((TransactionContainer) container);
            }
            byteValue = new Byte(TX_NOT_SUPPORTED);

        } else if (transAttribute.equalsIgnoreCase("Required")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequired((TransactionContainer) container);
            }
            byteValue = new Byte(TX_REQUIRED);

        } else if (transAttribute.equalsIgnoreCase("Never")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNever((TransactionContainer) container);
            }
            byteValue = new Byte(TX_NEVER);
        } else {
            throw new IllegalArgumentException("Invalid transaction attribute \"" + transAttribute + "\" declared for method " + method.getName() + ". Please check your configuration.");
        }

        /* EJB 1.1 page 55
         Only a stateful Session bean with container-managed transaction demarcation may implement the
         SessionSynchronization interface. A stateless Session bean must not implement the SessionSynchronization
         interface.
         */

        if (componentType == BeanType.STATEFUL && !isBeanManagedTransaction && container instanceof TransactionContainer) {

            if (SessionSynchronization.class.isAssignableFrom(beanClass)) {
                if (!transAttribute.equals("Never") && !transAttribute.equals("NotSupported")) {

                    policy = new SessionSynchronizationTxPolicy(policy);
                }
            } else {

                policy = new StatefulContainerManagedTxPolicy(policy);
            }
        }

        /**
           Only the NOT_SUPPORTED and REQUIRED transaction attributes may be used for message-driven
           bean message listener methods. The use of the other transaction attributes is not meaningful
           for message-driven bean message listener methods because there is no pre-existing client transaction
           context(REQUIRES_NEW, SUPPORTS) and no client to handle exceptions (MANDATORY, NEVER).
         */
        if (componentType.isMessageDriven() && !isBeanManagedTransaction && container instanceof TransactionContainer) {
            if (policy.getPolicyType() != TransactionPolicy.Type.NotSupported && policy.getPolicyType() != TransactionPolicy.Type.Required) {

                if (method.equals(this.ejbTimeout) && policy.getPolicyType() == TransactionPolicy.Type.RequiresNew) {
                    // do nothing. This is allowed as the timer callback method for a message driven bean
                    // can also have a transaction policy of RequiresNew Sec 5.4.12 of Ejb 3.0 Core Spec
                } else {
                    throw new OpenEJBException("The transaction attribute " + policy.policyToString() + "is not supported for the method "
                                               + method.getName() + " of the Message Driven Bean " + beanClass.getName());
                }
            }
        }
        methodTransactionAttributes.put(method, byteValue);
        methodTransactionPolicies.put(method, policy);
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

    public List<InterceptorData> getMethodInterceptors(Method method) {
        List<InterceptorData> interceptors = methodInterceptors.get(method);
        if (interceptors == null) {
            interceptors = new ArrayList<InterceptorData>();
        }
        return interceptors;
    }


    public void setMethodInterceptors(Method method, List<InterceptorData> interceptors) {
        methodInterceptors.put(method, interceptors);
        this.interceptors.addAll(interceptors);
    }

    private final Set<InterceptorData> interceptors = new HashSet<InterceptorData>();

    public Set<InterceptorData> getAllInterceptors() {
        return interceptors;
    }

    public List<InterceptorData> getCallbackInterceptors() {
        return callbackInterceptors;
    }

    public void setCallbackInterceptors(List<InterceptorData> callbackInterceptors) {
        this.callbackInterceptors.clear();
        this.callbackInterceptors.addAll(callbackInterceptors);
        this.interceptors.addAll(callbackInterceptors);
    }

    private javax.ejb.EJBHome createEJBHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_HOME);
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_HOME);
                break;
            case CMP_ENTITY:
            case BMP_ENTITY:
                handler = new EntityEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_HOME);
                break;
        }

        Object proxy = null;
        try {
            Class[] interfaces = new Class[]{this.getHomeInterface(), org.apache.openejb.core.ivm.IntraVmProxy.class};
            proxy = ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
        }

        return (javax.ejb.EJBHome) proxy;

    }

    private javax.ejb.EJBLocalHome createEJBLocalHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_LOCAL_HOME);
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_LOCAL_HOME);
                break;
            case CMP_ENTITY:
            case BMP_ENTITY:
                handler = new EntityEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.EJB_LOCAL_HOME);
                break;
        }
        handler.setLocal(true);
        Object proxy = null;
        try {
            Class[] interfaces = new Class[]{this.getLocalHomeInterface(), org.apache.openejb.core.ivm.IntraVmProxy.class};
            proxy = ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create EJBLocalHome stub" + e.getMessage());
        }

        return (javax.ejb.EJBLocalHome) proxy;
    }

    private BusinessLocalHome createBusinessLocalHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.BUSINESS_LOCAL_HOME);
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.BUSINESS_LOCAL_HOME);
                break;
        }
        handler.setLocal(true);
        try {
            Class[] interfaces = new Class[]{BusinessLocalHome.class, org.apache.openejb.core.ivm.IntraVmProxy.class};
            return (BusinessLocalHome) ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create BusinessLocalHome stub" + e.getMessage());
        }
    }

    private BusinessRemoteHome createBusinessRemoteHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.BUSINESS_REMOTE_HOME);
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID(), InterfaceType.BUSINESS_REMOTE_HOME);
                break;
        }
        try {
            Class[] interfaces = new Class[]{BusinessRemoteHome.class, org.apache.openejb.core.ivm.IntraVmProxy.class};
            return (BusinessRemoteHome) ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create BusinessRemoteHome stub" + e.getMessage());
        }
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
        if (businessLocal != null){
            mapObjectInterface(businessLocal);
        }
        if (businessRemote != null){
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
            if (componentType == BeanType.STATEFUL ) {

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
                methodMap.put(clientMethod, beanMethod);

                clientMethod = EJBHome.class.getDeclaredMethod("remove", java.lang.Object.class);
                methodMap.put(clientMethod, beanMethod);

                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove");
                methodMap.put(clientMethod, beanMethod);
            } else if (componentType == BeanType.BMP_ENTITY || componentType == BeanType.CMP_ENTITY) {
                Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove");
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", javax.ejb.Handle.class);
                methodMap.put(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", java.lang.Object.class);
                methodMap.put(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove");
                methodMap.put(clientMethod, beanMethod);
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
        for (int i = 0; i < homeMethods.length; i++) {
            Method method = homeMethods[i];
            Class owner = method.getDeclaringClass();
            if (owner == javax.ejb.EJBHome.class || owner == EJBLocalHome.class) {
                continue;
            }

            try {
                Method beanMethod = null;
                if (method.getName().startsWith("create")) {
                    StringBuilder ejbCreateName = new StringBuilder(method.getName());
                    ejbCreateName.replace(0,1, "ejbC");
                    beanMethod = beanClass.getMethod(ejbCreateName.toString(), method.getParameterTypes());
                    createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching
                    parameters. This code maps that relationship.
                    */
                    if (this.componentType == BeanType.BMP_ENTITY || this.componentType == BeanType.CMP_ENTITY) {
                        ejbCreateName.insert(3, "Post");
                        Method postCreateMethod = beanClass.getMethod(ejbCreateName.toString(), method.getParameterTypes());
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
                    methodMap.put(homeMethods[i], beanMethod);
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
        if (intrface == BusinessLocalHome.class || intrface == BusinessRemoteHome.class){
            return;
        }

        Method [] interfaceMethods = intrface.getMethods();
        for (int i = 0; i < interfaceMethods.length; i++) {
            Method method = interfaceMethods[i];
            Class declaringClass = method.getDeclaringClass();
            if (declaringClass == javax.ejb.EJBObject.class || declaringClass == EJBLocalObject.class) {
                continue;
            }
            try {
                Method beanMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());
                methodMap.put(method, beanMethod);
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException("Invalid method [" + method + "]. Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    public Class getObjectInterface(Class homeInterface){
        if (BusinessLocalHome.class.isAssignableFrom(homeInterface)){
            return getBusinessLocalInterface();
        } else if (BusinessRemoteHome.class.isAssignableFrom(homeInterface)){
            return getBusinessRemoteInterface();
        } else if (EJBLocalHome.class.isAssignableFrom(homeInterface)){
            return getLocalInterface();
        } else if (EJBHome.class.isAssignableFrom(homeInterface)){
            return getRemoteInterface();
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
}
