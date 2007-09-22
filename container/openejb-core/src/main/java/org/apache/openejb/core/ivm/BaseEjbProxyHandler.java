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
package org.apache.openejb.core.ivm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.NotSerializableException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.AccessLocalException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.proxy.InvocationHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public abstract class BaseEjbProxyHandler implements InvocationHandler, Serializable {
    private static final String OPENEJB_LOCALCOPY = "openejb.localcopy";

    private static class ProxyRegistry {

        protected final Hashtable liveHandleRegistry = new Hashtable();
    }

    public final Object deploymentID;

    public final Object primaryKey;

    public boolean inProxyMap = false;

    private transient WeakReference<CoreDeploymentInfo> deploymentInfo;

    public transient RpcContainer container;

    protected boolean isInvalidReference = false;

    /*
    * The EJB 1.1 specification requires that arguments and return values between beans adhere to the
    * Java RMI copy semantics which requires that the all arguments be passed by value (copied) and 
    * never passed as references.  However, it is possible for the system administrator to turn off the
    * copy operation so that arguments and return values are passed by reference as performance optimization.
    * Simply setting the org.apache.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause this variable to
    * set to false, and therefor bypass the copy operations in the invoke( ) method of this class; arguments
    * and return values will be passed by reference not value. 
    *
    * This property is, by default, always TRUE but it can be changed to FALSE by setting it as a System property
    * or a property of the Property argument when invoking OpenEJB.init(props).  This variable is set to that
    * property in the static block for this class.
    */
    protected boolean doIntraVmCopy;
    protected boolean doCrossClassLoaderCopy;
    private static final boolean REMOTE_COPY_ENABLED = parseRemoteCopySetting();
    protected final InterfaceType interfaceType;
    private transient WeakHashMap<Class,Object> interfaces;
    private transient WeakReference<Class> mainInterface;

    public BaseEjbProxyHandler(DeploymentInfo deploymentInfo, Object pk, InterfaceType interfaceType, List<Class> interfaces) {
        this.container = (RpcContainer) deploymentInfo.getContainer();
        this.deploymentID = deploymentInfo.getDeploymentID();
        this.interfaceType = interfaceType;
        this.primaryKey = pk;
        this.setDeploymentInfo((CoreDeploymentInfo) deploymentInfo);

        if (interfaces == null || interfaces.size() == 0) {
            InterfaceType objectInterfaceType = (interfaceType.isHome()) ? interfaceType.getCounterpart() : interfaceType;
            interfaces = new ArrayList<Class>(deploymentInfo.getInterfaces(objectInterfaceType));
        }

        this.doIntraVmCopy = !interfaceType.isLocal();

        if (!interfaceType.isLocal()){
            doIntraVmCopy = REMOTE_COPY_ENABLED;
        }

        setInterfaces(interfaces);

        if (interfaceType.isHome()){
            setMainInterface(deploymentInfo.getInterface(interfaceType));
        } else {
            // Then arbitrarily pick the first interface
            setMainInterface(interfaces.get(0));
        }
    }

    /**
     * This method should be called to determine the corresponding
     * business interface class to name as the invoking interface.
     * This method should NOT be called on non-business-interface
     * methods the proxy has such as java.lang.Object or IntraVmProxy.
     * @param method
     * @return the business (or component) interface matching this method
     */
    protected Class<?> getInvokedInterface(Method method) {
        // Home's only have one interface ever.  We don't
        // need to verify that the method invoked is in
        // it's interface.
        Class mainInterface = getMainInterface();
        if (interfaceType.isHome()) return mainInterface;

        Class declaringClass = method.getDeclaringClass();

        // If our "main" interface is or extends the method's declaring class
        // then we're good.  We know the main interface has the method being
        // invoked and it's safe to return it as the invoked interface.
        if (declaringClass.isAssignableFrom(mainInterface)){
            return mainInterface;
        }

        // If the method being invoked isn't in the "main" interface
        // we need to find a suitable interface or throw an exception.
        for (Class secondaryInterface : interfaces.keySet()) {
            if (declaringClass.isAssignableFrom(secondaryInterface)){
                return secondaryInterface;
            }
        }

        // We couldn't find an implementing interface.  Where did this
        // method come from???  Freak occurence.  Throw an exception.
        throw new IllegalStateException("Received method invocation and cannot determine corresponding business interface: method=" + method);
    }

    public Class getMainInterface() {
        return mainInterface.get();
    }

    private void setMainInterface(Class referent) {
        mainInterface = new WeakReference<Class>(referent);
    }

    private void setInterfaces(List<Class> interfaces) {
        this.interfaces = new WeakHashMap<Class,Object>(interfaces.size());
        for (Class clazz : interfaces) {
            this.interfaces.put(clazz, null);
        }
    }

    public List<Class> getInterfaces() {
        Set<Class> classes = interfaces.keySet();
        return new ArrayList(classes);
    }

    private static boolean parseRemoteCopySetting() {
        Properties properties = SystemInstance.get().getProperties();
        String value = properties.getProperty(OPENEJB_LOCALCOPY);
        if (value == null) {
            value = properties.getProperty(org.apache.openejb.core.EnvProps.INTRA_VM_COPY);
        }
        return value == null || !value.equalsIgnoreCase("FALSE");
    }

    protected void checkAuthorization(Method method) throws org.apache.openejb.OpenEJBException {
    }

    public void setIntraVmCopyMode(boolean on) {
        doIntraVmCopy = on;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isInvalidReference) {
            if (interfaceType.isComponent() && interfaceType.isLocal()){
                throw new NoSuchObjectLocalException("reference is invalid");
            } else if (interfaceType.isComponent() || java.rmi.Remote.class.isAssignableFrom(method.getDeclaringClass())) {
                throw new NoSuchObjectException("reference is invalid");
            } else {
                throw new javax.ejb.NoSuchEJBException("reference is invalid");
            }
        }
        getDeploymentInfo(); // will throw an exception if app has been undeployed.

        if (method.getDeclaringClass() == Object.class) {
            final String methodName = method.getName();

            if (methodName.equals("toString")) return toString();
            else if (methodName.equals("equals")) return equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
            else if (methodName.equals("hashCode")) return new Integer(hashCode());
            else
                throw new UnsupportedOperationException("Unkown method: " + method);
        } else if (method.getDeclaringClass() == IntraVmProxy.class) {
            final String methodName = method.getName();

            if (methodName.equals("writeReplace")) return _writeReplace(proxy);
            else
                throw new UnsupportedOperationException("Unkown method: " + method);
        }

        Class interfce = getInvokedInterface(method);

        // Should we copy arguments as required by the specification?
        if (doIntraVmCopy && !doCrossClassLoaderCopy) {

            if (args != null && args.length > 0) {
                IntraVmCopyMonitor.preCopyOperation();
                try {
                    args = copyArgs(args);
                } finally {
                    IntraVmCopyMonitor.postCopyOperation();
                }
            }
            Object returnObj = null;
            try {
                returnObj = _invoke(proxy, interfce, method, args);
            } catch (Throwable throwable) {
                // exceptions are return values and must be coppied
                IntraVmCopyMonitor.preCopyOperation();
                try {
                    throwable = (Throwable) copyObj(throwable);
                    throw convertException(throwable, method, interfce);
                } finally {
                    IntraVmCopyMonitor.postCopyOperation();
                }
            }

            if (returnObj != null) {
                IntraVmCopyMonitor.preCopyOperation();
                try {
                    returnObj = copyObj(returnObj);
                } finally {
                    IntraVmCopyMonitor.postCopyOperation();
                }
            }
            return returnObj;

        } else if (doIntraVmCopy) {
            // copy method and arguments to EJB's class loader
            IntraVmCopyMonitor.preCrossClassLoaderOperation();
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getDeploymentInfo().getClassLoader());
            try {
                if (args != null && args.length > 0) {
                    args = copyArgs(args);
                }
                method = copyMethod(method);
                interfce = (Class) copyObj(interfce);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
                IntraVmCopyMonitor.postCrossClassLoaderOperation();
            }

            // invoke method
            Object returnObj = null;
            try {
                returnObj = _invoke(proxy, interfce, method, args);
            } catch (Throwable throwable) {
                // exceptions are return values and must be coppied
                IntraVmCopyMonitor.preCrossClassLoaderOperation();
                try {
                    throwable = (Throwable) copyObj(throwable);
                    throw convertException(throwable, method, interfce);
                } finally {
                    IntraVmCopyMonitor.postCrossClassLoaderOperation();
                }
            }

            if (returnObj != null) {
                IntraVmCopyMonitor.preCrossClassLoaderOperation();
                try {
                    returnObj = copyObj(returnObj);
                } finally {
                    IntraVmCopyMonitor.postCrossClassLoaderOperation();
                }
            }
            return returnObj;
        } else {
            try {
                /*
                     * The EJB 1.1 specification requires that arguments and return values between beans adhere to the
                     * Java RMI copy semantics which requires that the all arguments be passed by value (copied) and
                     * never passed as references.  However, it is possible for the system administrator to turn off the
                     * copy operation so that arguments and return values are passed by reference as a performance optimization.
                     * Simply setting the org.apache.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause
                     * IntraVM to bypass the copy operations; arguments and return values will be passed by reference not value.
                     * This property is, by default, always TRUE but it can be changed to FALSE by setting it as a System property
                     * or a property of the Property argument when invoking OpenEJB.init(props).  The doIntraVmCopy variable is set to that
                     * property in the static block for this class.
                     */

                return _invoke(proxy, interfce, method, args);
            } catch (Throwable t) {
                throw convertException(t, method, interfce);
            }
        }
    }

    /**
     * Renamed method so it shows up with a much more understandable purpose as it
     * will be the top element in the stacktrace
     * @param e
     * @param method
     * @param interfce
     */
    protected Throwable convertException(Throwable e, Method method, Class interfce) {
        boolean rmiRemote = java.rmi.Remote.class.isAssignableFrom(interfce);
        if (e instanceof TransactionRequiredException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new EJBTransactionRequiredException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new TransactionRequiredLocalException(e.getMessage()).initCause(getCause(e));
            } else {
                return e;
            }
        }
        if (e instanceof TransactionRolledbackException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new EJBTransactionRolledbackException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new TransactionRolledbackLocalException(e.getMessage()).initCause(getCause(e));
            } else {
                return e;
            }
        }
        if (e instanceof NoSuchObjectException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new NoSuchEJBException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new NoSuchObjectLocalException(e.getMessage()).initCause(getCause(e));
            } else {
                return e;
            }
        }
        if (e instanceof RemoteException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new EJBException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new EJBException(e.getMessage()).initCause(getCause(e));
            } else {
                return e;
            }
        }
        if (e instanceof AccessException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new AccessLocalException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new AccessLocalException(e.getMessage()).initCause(getCause(e));
            } else {
                return e;
            }
        }

        for (Class<?> type : method.getExceptionTypes()) {
            if (type.isAssignableFrom(e.getClass())) {
                return e;
            }
        }

        // Exception is undeclared
        // Try and find a runtime exception in there
        while (e.getCause() != null && !(e instanceof RuntimeException)) {
            e = e.getCause();
        }
        return e;
    }

    /**
     * Method instance on proxies that come from a classloader outside
     * the bean's classloader need to be swapped out for the identical
     * method in the bean's classloader.
     *
     * @param method
     * @return return's the same method but loaded from the beans classloader
     */

    private Method copyMethod(Method method) throws Exception {
        int parameterCount = method.getParameterTypes().length;
        Object[] types = new Object[1 + parameterCount];
        types[0] = method.getDeclaringClass();
        System.arraycopy(method.getParameterTypes(), 0, types, 1, parameterCount);

        types = copyArgs(types);

        Class targetClass = (Class) types[0];
        Class[] targetParameters = new Class[parameterCount];
        System.arraycopy(types, 1, targetParameters, 0, parameterCount);
        Method targetMethod = targetClass.getMethod(method.getName(), targetParameters);
        return targetMethod;
    }

    protected Throwable getCause(Throwable e) {
        if (e != null && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    public String toString() {
        String name = null;
        try {
            name = getProxyInfo().getInterface().getName();
        } catch (Exception e) {
        }
        return "proxy=" + name + ";deployment=" + this.deploymentID + ";pk=" + this.primaryKey;
    }

    public int hashCode() {
        if (primaryKey == null) {

            return deploymentID.hashCode();
        } else {
            return primaryKey.hashCode();
        }
    }

    public boolean equals(Object obj) {
        try {
            obj = ProxyManager.getInvocationHandler(obj);
        } catch (IllegalArgumentException e) {
            return false;
        }
        BaseEjbProxyHandler other = (BaseEjbProxyHandler) obj;
        if (primaryKey == null) {
            return other.primaryKey == null && deploymentID.equals(other.deploymentID);
        } else {
            return primaryKey.equals(other.primaryKey) && deploymentID.equals(other.deploymentID);
        }
    }

    protected abstract Object _invoke(Object proxy, Class interfce, Method method, Object[] args) throws Throwable;

    protected Object[] copyArgs(Object[] objects) throws IOException, ClassNotFoundException {
        /* 
            while copying the arguments is necessary. Its not necessary to copy the array itself,
            because they array is created by the Proxy implementation for the sole purpose of 
            packaging the arguments for the InvocationHandler.invoke( ) method. Its ephemeral
            and their for doesn't need to be copied.
        */

        for (int i = 0; i < objects.length; i++) {
            objects[i] = copyObj(objects[i]);
        }

        return objects;
    }

    /* change dereference to copy */
    protected Object copyObj(Object object) throws IOException, ClassNotFoundException {
    	// Check for primitive and other known class types that are immutable.  If detected
    	// we can safely return them.
    	if (object == null) return null;
    	Class ooc = object.getClass();
        if ((ooc == int.class         ) ||
            (ooc == String.class      ) ||
            (ooc == long.class        ) ||
            (ooc == boolean.class     ) ||
            (ooc == byte.class        ) ||
            (ooc == float.class       ) ||
            (ooc == double.class      ) ||
            (ooc == short.class       ) ||
            (ooc == Long.class        ) ||
            (ooc == Boolean.class     ) ||
            (ooc == Byte.class        ) ||
            (ooc == Character.class   ) ||
            (ooc == Float.class       ) ||
            (ooc == Double.class      ) ||
            (ooc == Short.class       ) ||
            (ooc == BigDecimal.class  ))
        {
            return object;
        }


        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(128);
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.close();
        } catch (NotSerializableException e) {
            throw (IOException) new NotSerializableException(e.getMessage()+" : The EJB specification restricts remote interfaces to only serializable data types.  This can be disabled for in-vm use with the "+OPENEJB_LOCALCOPY+"=false system property.").initCause(e);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new EjbObjectInputStream(bais);
        Object obj = in.readObject();
        return obj;
    }

    public void invalidateReference() {
        this.container = null;
        this.setDeploymentInfo(null);
        this.isInvalidReference = true;
    }

    protected void invalidateAllHandlers(Object key) {
        HashSet<BaseEjbProxyHandler> set = (HashSet) getLiveHandleRegistry().remove(key);
        if (set == null) return;
        synchronized (set) {
            for (BaseEjbProxyHandler handler : set) {
                handler.invalidateReference();
            }
        }
    }

    protected abstract Object _writeReplace(Object proxy) throws ObjectStreamException;

    protected void registerHandler(Object key, BaseEjbProxyHandler handler) {
        HashSet set = (HashSet) getLiveHandleRegistry().get(key);
        if (set != null) {
            synchronized (set) {
                set.add(handler);
            }
        } else {
            set = new HashSet();
            set.add(handler);
            getLiveHandleRegistry().put(key, set);
        }
    }

    public abstract org.apache.openejb.ProxyInfo getProxyInfo();

    public CoreDeploymentInfo getDeploymentInfo() {
        CoreDeploymentInfo info = deploymentInfo.get();
        if (info == null|| info.isDestroyed()){
            invalidateReference();
            throw new IllegalStateException("Bean '"+deploymentID+"' has been undeployed.");
        }
        return info;
    }

    public void setDeploymentInfo(CoreDeploymentInfo deploymentInfo) {
        this.deploymentInfo = new WeakReference<CoreDeploymentInfo>(deploymentInfo);
    }

    public Hashtable getLiveHandleRegistry() {
        CoreDeploymentInfo deploymentInfo = getDeploymentInfo();
        ProxyRegistry proxyRegistry = deploymentInfo.get(ProxyRegistry.class);
        if (proxyRegistry == null){
            proxyRegistry = new ProxyRegistry();
            deploymentInfo.set(ProxyRegistry.class, proxyRegistry);
        }
        return proxyRegistry.liveHandleRegistry;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeObject(getInterfaces());
        out.writeObject(getMainInterface());
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {

        in.defaultReadObject();

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        setDeploymentInfo((CoreDeploymentInfo) containerSystem.getDeploymentInfo(deploymentID));
        container = (RpcContainer) getDeploymentInfo().getContainer();

        if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            doCrossClassLoaderCopy = true;
        }

        setInterfaces((List<Class>) in.readObject());
        setMainInterface((Class) in.readObject());
    }

}
