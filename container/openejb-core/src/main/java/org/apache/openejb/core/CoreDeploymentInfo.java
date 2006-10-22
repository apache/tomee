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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.SessionSynchronization;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;

import org.apache.openejb.Container;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.SystemException;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.BeanType;
import org.apache.openejb.alt.containers.castor_cmp11.CastorCmpEntityTxPolicy;
import org.apache.openejb.alt.containers.castor_cmp11.KeyGenerator;
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
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.util.proxy.ProxyManager;

/**
 * @org.apache.xbean.XBean element="deployment"
 */
public class CoreDeploymentInfo implements org.apache.openejb.DeploymentInfo {

    private Class homeInterface;
    private Class remoteInterface;
    private Class localHomeInterface;
    private Class localInterface;
    private Class beanClass;
    private Class pkClass;
    private Class businessLocal;
    private Class businessRemote;

    private Method postConstruct;
    private Method preDestroy;
    private Method prePassivate;
    private Method postActivate;

    private boolean isBeanManagedTransaction;
    private boolean isReentrant;
    private Container container;
    private URL archiveURL;
    private EJBHome ejbHomeRef;
    private EJBLocalHome ejbLocalHomeRef;
    private BusinessLocalHome businessLocalHomeRef;
    private BusinessRemoteHome businessRemoteHomeRef;

    private Object containerData;

    private final DeploymentContext context;

    private Method createMethod = null;

    private HashMap postCreateMethodMap = new HashMap();
    private final BeanType componentType;

    private HashMap methodPermissions = new HashMap();
    private HashMap methodTransactionAttributes = new HashMap();
    private HashMap methodTransactionPolicies = new HashMap();
    private HashMap methodMap = new HashMap();
    private HashMap securityRoleReferenceMap = new HashMap();
    private String jarPath;

    public CoreDeploymentInfo(DeploymentContext context,
                              String beanClass, String homeInterface,
                              String remoteInterface,
                              String localHomeInterface,
                              String localInterface,
                              String businessLocal, String businessRemote, String pkClass,
                              String ejbType,
                              ClassLoader classLoader) throws SystemException {
        this(context,
                loadClass(beanClass, classLoader), loadClass(homeInterface, classLoader),
                loadClass(remoteInterface, classLoader),
                loadClass(localHomeInterface, classLoader),
                loadClass(localInterface, classLoader),
                loadClass(businessLocal, classLoader),
                loadClass(businessRemote, classLoader),
                loadClass(pkClass, classLoader),
                getComponentType(ejbType), null);
    }


    private static Class loadClass(String name, ClassLoader classLoader) throws SystemException {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
    }

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
            default: throw new IllegalStateException("Unexpected enum constant: " + interfaceType);
        }
    }

    private static BeanType getComponentType(String name) throws SystemException {
        if ("cmp".equalsIgnoreCase(name)) {
            return BeanType.CMP_ENTITY;
        } else if ("bmp".equalsIgnoreCase(name)) {
            return BeanType.BMP_ENTITY;
        } else if ("stateful".equalsIgnoreCase(name)) {
            return BeanType.STATEFUL;
        } else if ("stateless".equalsIgnoreCase(name)) {
            return BeanType.STATELESS;
        } else {
            throw new SystemException("Unknown component type: " + name);
        }
    }

    public CoreDeploymentInfo(DeploymentContext context,
                              Class beanClass, Class homeInterface,
                              Class remoteInterface,
                              Class localHomeInterface,
                              Class localInterface,
                              Class businessLocal, Class businessRemote, Class pkClass,
                              BeanType componentType,
                              URL archiveURL) throws SystemException {

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
        this.archiveURL = archiveURL;

//        if (businessLocal != null && localHomeInterface == null){
//            this.localHomeInterface = BusinessLocalHome.class;
//        }
//
//        if (businessRemote != null && homeInterface == null){
//            this.homeInterface = BusinessRemoteHome.class;
//        }

        if (SessionBean.class.isAssignableFrom(beanClass)){
            try {
                this.preDestroy = SessionBean.class.getMethod("ejbRemove");
                this.prePassivate = SessionBean.class.getMethod("ejbPassivate");
                this.postActivate = SessionBean.class.getMethod("ejbActivate");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
        createMethodMap();

        if (EnterpriseBean.class.isAssignableFrom(beanClass)){
            try {
                preDestroy = beanClass.getMethod("ejbRemove");
            } catch (NoSuchMethodException e) {
                throw new SystemException(e);
            }
        }
    }

    public Object getContainerData() {
        return containerData;
    }

    public void setContainerData(Object containerData) {
        this.containerData = containerData;
    }

    public void setContainer(Container cont) {
        container = cont;
    }

    public BeanType getComponentType() {
        return componentType;
    }

    public byte getTransactionAttribute(Method method) {

        Byte byteWrapper = (Byte) methodTransactionAttributes.get(method);
        if (byteWrapper == null)
            return TX_NOT_SUPPORTED;// non remote or home interface method
        else
            return byteWrapper.byteValue();
    }

    public TransactionPolicy getTransactionPolicy(Method method) {

        TransactionPolicy policy = (TransactionPolicy) methodTransactionPolicies.get(method);
        if (policy == null && !isBeanManagedTransaction) {
            org.apache.log4j.Logger.getLogger("OpenEJB").info("The following method doesn't have a transaction policy assigned: " + method);
        }
        if (policy == null && container instanceof TransactionContainer) {
            if (isBeanManagedTransaction) {
                if (componentType == BeanType.STATEFUL) {
                    policy = new StatefulBeanManagedTxPolicy((TransactionContainer) container);
                } else if (componentType == BeanType.STATELESS) {
                    policy = new StatelessBeanManagedTxPolicy((TransactionContainer) container);
                }
            } else if (componentType == BeanType.STATEFUL) {
                policy = new TxNotSupported((TransactionContainer) container);
                policy = new StatefulContainerManagedTxPolicy(policy);
            } else if (componentType == BeanType.CMP_ENTITY) {
                policy = new TxNotSupported((TransactionContainer) container);
                policy = new CastorCmpEntityTxPolicy(policy);
            } else {
                policy = new TxNotSupported((TransactionContainer) container);
            }
            methodTransactionPolicies.put(method, policy);
        }
        if (policy == null) policy = new NoTransactionPolicy();
        return policy ;
    }

    private static class NoTransactionPolicy extends TransactionPolicy {
        public void afterInvoke(Object bean, TransactionContext context) throws ApplicationException, SystemException {
        }

        public void beforeInvoke(Object bean, TransactionContext context) throws SystemException, ApplicationException {
        }

        public void handleApplicationException(Throwable appException, TransactionContext context) throws ApplicationException {
        }

        public void handleSystemException(Throwable sysException, Object instance, TransactionContext context) throws ApplicationException, SystemException {
        }
    }
    public String [] getAuthorizedRoles(Method method) {
        HashSet roleSet = (HashSet) methodPermissions.get(method);
        if (roleSet == null) return null;
        String [] roles = new String[roleSet.size()];
        return (String []) roleSet.toArray(roles);
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

    public javax.naming.Context getJndiEnc() {
        return context.getJndiContext();
    }

    public boolean isReentrant() {
        return isReentrant;
    }

    public void setIsReentrant(boolean reentrant) {
        isReentrant = reentrant;
    }

    public Method getMatchingBeanMethod(Method interfaceMethod) {
        Method mthd = (Method) methodMap.get(interfaceMethod);
        return (mthd == null) ? interfaceMethod : mthd;
    }

    public void appendMethodPermissions(Method m, String [] roleNames) {
        HashSet hs = (HashSet) methodPermissions.get(m);
        if (hs == null) {
            hs = new HashSet();// FIXME: Set appropriate load and intial capacity
            methodPermissions.put(m, hs);
        }
        for (int i = 0; i < roleNames.length; i++) {
            hs.add(roleNames[i]);
        }
    }

    public String [] getPhysicalRole(String securityRoleReference) {
        return (String[]) securityRoleReferenceMap.get(securityRoleReference);
    }

    public void addSecurityRoleReference(String securityRoleReference, String [] physicalRoles) {
        securityRoleReferenceMap.put(securityRoleReference, physicalRoles);
    }

    public void setMethodTransactionAttribute(Method method, String transAttribute) {
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

        } else if (componentType == BeanType.CMP_ENTITY) {
            policy = new CastorCmpEntityTxPolicy(policy);
        }
        methodTransactionAttributes.put(method, byteValue);
        methodTransactionPolicies.put(method, policy);
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

    private void createMethodMap() throws org.apache.openejb.SystemException {
        if (remoteInterface != null) {
            mapObjectInterface(remoteInterface, false);
            mapHomeInterface(homeInterface);
        }
        if (localInterface != null) {
            mapObjectInterface(localInterface, true);
            mapHomeInterface(localHomeInterface);
        }


        try {

            if (componentType == BeanType.STATEFUL || componentType == BeanType.STATELESS) {
                Method beanMethod = javax.ejb.SessionBean.class.getDeclaredMethod("ejbRemove", new Class []{});
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{javax.ejb.Handle.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{java.lang.Object.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
                methodMap.put(clientMethod, beanMethod);
            } else if (componentType == BeanType.BMP_ENTITY || componentType == BeanType.CMP_ENTITY) {
                Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove", new Class []{});
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{javax.ejb.Handle.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{java.lang.Object.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
                methodMap.put(clientMethod, beanMethod);
            }
        } catch (java.lang.NoSuchMethodException nsme) {
            throw new org.apache.openejb.SystemException(nsme);
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
                if (method.getName().equals("create")) {
                    beanMethod = beanClass.getMethod("ejbCreate", method.getParameterTypes());
                    createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching 
                    parameters. This code maps that relationship.
                    */
                    if (this.componentType == BeanType.BMP_ENTITY || this.componentType == BeanType.CMP_ENTITY) {
                        Method postCreateMethod = beanClass.getMethod("ejbPostCreate", method.getParameterTypes());
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

    private void mapObjectInterface(Class intrface, boolean isLocal) {
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
        if (methodName.equals("create"))
            return "ejbCreate";
        else if (methodName.startsWith("find"))
            return "ejbF" + methodName.substring(1);
        else
            return "ejbH" + methodName.substring(1);
    }

    public Method getCreateMethod() {
        return createMethod;
    }

    public Method getPostActivate() {
        return postActivate;
    }

    public void setPostActivate(Method postActivate) {
        this.postActivate = postActivate;
    }

    public Method getPostConstruct() {
        // TODO: Need to truly enforce the backwards compatibility rules
        return (postConstruct == null)? getCreateMethod(): postConstruct;
    }

    public void setPostConstruct(Method postConstruct) {
        this.postConstruct = postConstruct;
    }

    public Method getPreDestroy() {
        return preDestroy;
    }

    public void setPreDestroy(Method preDestroy) {
        this.preDestroy = preDestroy;
    }

    public Method getPrePassivate() {
        return prePassivate;
    }

    public void setPrePassivate(Method prePassivate) {
        this.prePassivate = prePassivate;
    }

    public Method getMatchingPostCreateMethod(Method createMethod) {
        return (Method) this.postCreateMethodMap.get(createMethod);
    }

    private KeyGenerator keyGenerator;
    private Field primKeyField;
    private String[] cmrFields;

    private HashMap queryMethodMap = new HashMap();

    public Field getPrimaryKeyField() {
        return primKeyField;
    }

    public void setPrimKeyField(String fieldName)
            throws java.lang.NoSuchFieldException {
        if (componentType == BeanType.CMP_ENTITY) {

            primKeyField = beanClass.getField(fieldName);
        }
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
        return (String) queryMethodMap.get(queryMethod);
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }
}
