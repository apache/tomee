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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.AccessLocalException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.proxy.InvocationHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public abstract class BaseEjbProxyHandler implements InvocationHandler, Serializable {
    protected static final Hashtable liveHandleRegistry = new Hashtable();

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
    private boolean isLocal;
    protected final InterfaceType interfaceType;

    public BaseEjbProxyHandler(RpcContainer container, Object pk, Object depID, InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
        this.container = container;
        this.primaryKey = pk;
        this.deploymentID = depID;
        this.setDeploymentInfo((CoreDeploymentInfo) container.getDeploymentInfo(depID));

        Properties properties = SystemInstance.get().getProperties();
        String value = properties.getProperty("openejb.localcopy");
        if (value == null) {
            value = properties.getProperty(org.apache.openejb.core.EnvProps.INTRA_VM_COPY);
        }
        doIntraVmCopy = value == null || !value.equalsIgnoreCase("FALSE");
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException, NoSuchMethodException {

        in.defaultReadObject();

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        setDeploymentInfo((CoreDeploymentInfo) containerSystem.getDeploymentInfo(deploymentID));
        container = (RpcContainer) getDeploymentInfo().getContainer();

        if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            doCrossClassLoaderCopy = true;
        }
    }

    protected void checkAuthorization(Method method) throws org.apache.openejb.OpenEJBException {
        // todo can't really do auth check here
//        Object caller = getThreadSpecificSecurityIdentity();
//        boolean authorized = getSecurityService().isCallerAuthorized(method, null);
//        if (!authorized)
//            throw new org.apache.openejb.ApplicationException(new RemoteException("Unauthorized Access by Principal Denied"));
    }

    private SecurityService getSecurityService() {
        return SystemInstance.get().getComponent(SecurityService.class);
    }

    protected Object getThreadSpecificSecurityIdentity() {
        ThreadContext context = ThreadContext.getThreadContext();
        if (context != null) {
            return context.getSecurityIdentity();
        } else {
            return getSecurityService().getSecurityIdentity();
        }
    }

    public void setIntraVmCopyMode(boolean on) {
        doIntraVmCopy = on;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isInvalidReference) throw new NoSuchObjectException("reference is invalid");
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


        String jndiEnc = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
//        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES,"org.apache.openejb.core.ivm.naming");

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
            Object returnObj = _invoke(proxy, method, args);

            IntraVmCopyMonitor.preCopyOperation();
            try {
                returnObj = copyObj(returnObj);
            } finally {
                IntraVmCopyMonitor.postCopyOperation();
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
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
                IntraVmCopyMonitor.postCrossClassLoaderOperation();
            }

            // invoke method
            Object returnObj = _invoke(proxy, method, args);

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

                return _invoke(proxy, method, args);
            } catch (TransactionRequiredException e) {
                if (this.isLocal()) {
                    throw new TransactionRequiredLocalException(e.getMessage()).initCause(getCause(e));
                } else {
                    throw e;
                }
            } catch (TransactionRolledbackException e) {
                if (this.isLocal()) {
                    throw new TransactionRolledbackLocalException(e.getMessage()).initCause(getCause(e));
                } else {
                    throw e;
                }
            } catch (NoSuchObjectException  e) {
                if (this.isLocal()) {
                    throw new NoSuchObjectLocalException(e.getMessage()).initCause(getCause(e));
                } else {
                    throw e;
                }
            } catch (AccessException e) {
                if (this.isLocal()) {
                    throw new AccessLocalException(e.getMessage()).initCause(getCause(e));
                } else {
                    throw e;
                }
            } catch (RemoteException e) {
                if (this.isLocal()) {
                    throw new EJBException(e.getMessage()).initCause(getCause(e));
                } else {
                    throw e;
                }
            } catch (Throwable t) {
                //t.printStackTrace();
                Class[] etypes = method.getExceptionTypes();
                for (int i = 0; i < etypes.length; i++) {

                    if (etypes[i].isAssignableFrom(t.getClass())) {
                        throw t;
                    }
                }
                // Exception is undeclared
                // Try and find a runtime exception in there
                while (t.getCause() != null && !(t instanceof RuntimeException)) {
                    t = t.getCause();
                }
                throw t;
            }
        }
    }

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

    private Throwable getCause(RemoteException e) {
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

    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(object);
        out.close();

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

    protected static void invalidateAllHandlers(Object key) {
        HashSet<BaseEjbProxyHandler> set = (HashSet) liveHandleRegistry.remove(key);
        if (set == null) return;
        synchronized (set) {
            for (BaseEjbProxyHandler handler : set) {
                handler.invalidateReference();
            }
        }
    }

    protected abstract Object _writeReplace(Object proxy) throws ObjectStreamException;

    protected static void registerHandler(Object key, BaseEjbProxyHandler handler) {
        HashSet set = (HashSet) liveHandleRegistry.get(key);
        if (set != null) {
            synchronized (set) {
                set.add(handler);
            }
        } else {
            set = new HashSet();
            set.add(handler);
            liveHandleRegistry.put(key, set);
        }
    }

    public abstract org.apache.openejb.ProxyInfo getProxyInfo();

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
        this.doIntraVmCopy = !isLocal;
    }

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
}
