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
package org.apache.openejb.proxy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.DeploymentNotFoundException;
import org.apache.openejb.dispatch.InterfaceMethodSignature;

public class EJBProxyFactory {
    private static final Class[] sessionBaseClasses =
            new Class[]{SessionEJBObject.class, SessionEJBHome.class, SessionEJBLocalObject.class, SessionEJBLocalHome.class};

    private static final Class[] entityBaseClasses =
            new Class[]{EntityEJBObject.class, EntityEJBHome.class, EntityEJBLocalObject.class, EntityEJBLocalHome.class};

    private final String containerId;
    private final boolean isSessionBean;
    private final Class remoteInterface;
    private final Class homeInterface;
    private final Class localInterface;
    private final Class localHomeInterface;

    private transient final CglibEJBProxyFactory remoteFactory;
    private transient final CglibEJBProxyFactory homeFactory;
    private transient final CglibEJBProxyFactory localFactory;
    private transient final CglibEJBProxyFactory localHomeFactory;

    private transient RpcEjbDeployment container;

    private transient int[] remoteMap;
    private transient int[] homeMap;
    private transient int[] localMap;
    private transient int[] localHomeMap;

    private transient Map legacyMethodMap;

    public EJBProxyFactory(RpcEjbDeployment container) {
        this(container.getProxyInfo());
        setContainer(container);
    }

    public EJBProxyFactory(ProxyInfo proxyInfo) {
        this(
                proxyInfo.getContainerID(),
                proxyInfo.isSessionBean(),
                proxyInfo.getRemoteInterface(),
                proxyInfo.getHomeInterface(),
                proxyInfo.getLocalInterface(),
                proxyInfo.getLocalHomeInterface());
    }

    public EJBProxyFactory(
            String containerId,
            boolean sessionBean,
            Class remoteInterface,
            Class homeInterface,
            Class localInterface,
            Class localHomeInterface) {
        this.containerId = containerId;
        isSessionBean = sessionBean;

// JNB: these are temporarily disabled due to classloader issues during deployment
//        assert remoteInterface == null || (remoteInterface.isInterface() && EJBObject.class.isAssignableFrom(remoteInterface));
        this.remoteInterface = remoteInterface;

//        assert homeInterface == null || (homeInterface.isInterface() && EJBHome.class.isAssignableFrom(homeInterface));
        this.homeInterface = homeInterface;

//        assert localInterface == null || (localInterface.isInterface() && EJBLocalObject.class.isAssignableFrom(localInterface));
        this.localInterface = localInterface;

//        assert localHomeInterface == null || (localHomeInterface.isInterface() && EJBLocalHome.class.isAssignableFrom(localHomeInterface));
        this.localHomeInterface = localHomeInterface;

        this.remoteFactory = getFactory(EJBInterfaceType.REMOTE.getOrdinal(), remoteInterface);
        this.homeFactory = getFactory(EJBInterfaceType.HOME.getOrdinal(), homeInterface);
        this.localFactory = getFactory(EJBInterfaceType.LOCAL.getOrdinal(), localInterface);
        this.localHomeFactory = getFactory(EJBInterfaceType.LOCALHOME.getOrdinal(), localHomeInterface);
    }

    public String getEJBName() {
        return container.getEjbName();
    }

    RpcEjbDeployment getContainer() throws DeploymentNotFoundException {
        if (container == null) {
            locateContainer();
        }
        return container;
    }

    private void setContainer(RpcEjbDeployment container) {
        assert container != null: "container is null";
        this.container = container;

        ProxyInfo proxyInfo = container.getProxyInfo();
        InterfaceMethodSignature[] signatures = container.getSignatures();

        // build the legacy map
        Map map = new HashMap();
        addLegacyMethods(map, proxyInfo.getRemoteInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getHomeInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getLocalInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getLocalHomeInterface(), signatures);
        addLegacyMethods(map, proxyInfo.getServiceEndpointInterface(), signatures);
        legacyMethodMap = Collections.unmodifiableMap(map);

        remoteMap = createOperationsMap(remoteFactory, signatures);
        homeMap = createOperationsMap(homeFactory, signatures);

        localMap = createOperationsMap(localFactory, signatures);
        localHomeMap = createOperationsMap(localHomeFactory, signatures);
    }

    public ClassLoader getClassLoader() {
        if (remoteFactory != null) {
            return remoteFactory.getType().getClassLoader();
        } else {
            return localFactory.getType().getClassLoader();
        }
    }

    int[] getOperationMap(EJBInterfaceType type) throws DeploymentNotFoundException {
        if (container == null) {
            locateContainer();
        }

        if (type == EJBInterfaceType.REMOTE) {
            return remoteMap;
        } else if (type == EJBInterfaceType.HOME) {
            return homeMap;
        } else if (type == EJBInterfaceType.LOCAL) {
            return localMap;
        } else if (type == EJBInterfaceType.LOCALHOME) {
            return localHomeMap;
        } else {
            throw new IllegalArgumentException("Unsupported interface type " + type);
        }
    }

    public int getMethodIndex(Method method) {
        Integer index = (Integer) legacyMethodMap.get(method);
        if (index == null) {
            index = new Integer(-1);
        }
        return index.intValue();
    }

    public Class getLocalInterfaceClass() {
        return localInterface;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterface;
    }

    /**
     * Return a proxy for the EJB's remote interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBObject() )
     * @return the proxy for this EJB's home interface
     */
    public EJBObject getEJBObject(Object primaryKey) {
        if (remoteFactory == null) {
            throw new IllegalStateException("getEJBObject is not allowed if no remote interface is defined");
        }
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.REMOTE,
                container,
                remoteMap,
                primaryKey);
        return (EJBObject) remoteFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's home interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to EJBContext.getEJBHome() )
     * @return the proxy for this EJB's home interface
     */
    public EJBHome getEJBHome() {
        if (homeFactory == null) {
            throw new IllegalStateException("getEJBHome is not allowed if no remote interface is defined");
        }
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.HOME,
                container,
                homeMap);
        return (EJBHome) homeFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's local interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBLocalObject() )
     * @return the proxy for this EJB's local interface
     */

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        if (localFactory == null) {
            throw new IllegalStateException("getEJBLocalObject is not allowed if no local interface is defined");
        }
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.LOCAL,
                container,
                localMap,
                primaryKey);
        return (EJBLocalObject) localFactory.create(handler);
    }

    /**
     * Return a proxy for the EJB's local home interface. This can be
     * passed back to any client that wishes to access the EJB
     * (e.g. in response to a call to EJBContext.getEJBLocalHome() )
     * @return the proxy for this EJB's local home interface
     */

    public EJBLocalHome getEJBLocalHome() {
        if (localFactory == null) {
            throw new IllegalStateException("getEJBLocalHome is not allowed if no local interface is defined");
        }
        EJBMethodInterceptor handler = new EJBMethodInterceptor(
                this,
                EJBInterfaceType.LOCALHOME,
                container,
                localHomeMap);
        return (EJBLocalHome) localHomeFactory.create(handler);
    }

    private int[] createOperationsMap(CglibEJBProxyFactory factory, InterfaceMethodSignature[] signatures) {
        if (factory == null) return new int[0];
        return EJBProxyHelper.getOperationMap(factory.getType(), signatures, false);
    }

    private CglibEJBProxyFactory getFactory(int interfaceType, Class interfaceClass) {
        if (interfaceClass == null) {
            return null;
        }

        Class baseClass;
        if (isSessionBean) {
            baseClass = sessionBaseClasses[interfaceType];
        } else {
            baseClass = entityBaseClasses[interfaceType];
        }

        ClassLoader classLoader = findClassLoader(baseClass, interfaceClass);

        return new CglibEJBProxyFactory(baseClass, interfaceClass, classLoader);
    }

    private ClassLoader findClassLoader(Class baseClass, Class interfaceClass) {
        ClassLoader cl = interfaceClass.getClassLoader();
        try {
            cl.loadClass(baseClass.getName());
            return cl;
        } catch (ClassNotFoundException e) {

        }
        cl = baseClass.getClassLoader();
        try {
            cl.loadClass(interfaceClass.getName());
            return cl;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Openejb base class: " + baseClass.getName() + " and interface class: " + interfaceClass.getName() + " do not have a common classloader that will load both!");
        }
    }

    private static void addLegacyMethods(Map legacyMethodMap, Class clazz, InterfaceMethodSignature[] signatures) {
        if (clazz == null) {
            return;
        }

        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            Method method = signature.getMethod(clazz);
            if (method != null) {
                legacyMethodMap.put(method, new Integer(i));
            }
        }
    }

    private void locateContainer() throws DeploymentNotFoundException {
        DeploymentIndex deploymentIndex = DeploymentIndex.getInstance();
        RpcEjbDeployment c = deploymentIndex.getDeployment(containerId);
        if (c == null) {
            throw new IllegalStateException("Contianer not found: " + containerId);
        }
        setContainer(c);
    }

    private Object readResolve() {
        return new EJBProxyFactory(
                containerId,
                isSessionBean,
                remoteInterface,
                homeInterface,
                localInterface,
                localHomeInterface);
    }
}
