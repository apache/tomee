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

package org.apache.openejb.core.ivm;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRequiredException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.TransactionRequiredLocalException;
import jakarta.ejb.TransactionRolledbackLocalException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.TransactionRolledbackException;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.openejb.core.ivm.IntraVmCopyMonitor.State.CLASSLOADER_COPY;
import static org.apache.openejb.core.ivm.IntraVmCopyMonitor.State.COPY;
import static org.apache.openejb.core.ivm.IntraVmCopyMonitor.State.NONE;

@SuppressWarnings("unchecked")
public abstract class BaseEjbProxyHandler implements InvocationHandler, Serializable {

    private static final String OPENEJB_LOCALCOPY = "openejb.localcopy";
    private static final boolean REMOTE_COPY_ENABLED = parseRemoteCopySetting();
    static {
        ThreadContext.addThreadContextListener(new ThreadContextListener() {
            @Override
            public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
                // no-op
            }

            @Override
            public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
                if (exitedContext != null) {
                    final ProxyRegistry proxyRegistry = exitedContext.get(ProxyRegistry.class);
                    if (proxyRegistry != null) {
                        proxyRegistry.liveHandleRegistry.clear();
                    }
                }
            }
        });
    }

    public final Object deploymentID;
    public final Object primaryKey;
    protected final InterfaceType interfaceType;
    private final ReentrantLock lock = new ReentrantLock();
    public boolean inProxyMap;
    public transient RpcContainer container;
    protected boolean isInvalidReference;
    protected Object clientIdentity;
    private IntraVmCopyMonitor.State strategy = NONE;
    private transient WeakReference<BeanContext> beanContextRef;
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
    private boolean doIntraVmCopy;
    private boolean doCrossClassLoaderCopy;
    private transient WeakHashMap<Class, Object> interfaces;
    private transient WeakReference<Class> mainInterface;

    public BaseEjbProxyHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, List<Class> interfaces, Class mainInterface) {
        this.container = (RpcContainer) beanContext.getContainer();
        this.deploymentID = beanContext.getDeploymentID();
        this.interfaceType = interfaceType;
        this.primaryKey = pk;
        this.setBeanContext(beanContext);

        if (interfaces == null || interfaces.size() == 0) {
            final InterfaceType objectInterfaceType = interfaceType.isHome() ? interfaceType.getCounterpart() : interfaceType;
            interfaces = new ArrayList<>(beanContext.getInterfaces(objectInterfaceType));
        }

        if (mainInterface == null && interfaces.size() == 1) {
            mainInterface = interfaces.get(0);
        }

        setInterfaces(interfaces);
        setMainInterface(mainInterface);
        if (mainInterface == null) {
            throw new IllegalArgumentException("No mainInterface: otherwise di: " + beanContext + " InterfaceType: " + interfaceType + " interfaces: " + interfaces);
        }
        this.setDoIntraVmCopy(REMOTE_COPY_ENABLED && !interfaceType.isLocal() && !interfaceType.isLocalBean());
    }

    private static boolean parseRemoteCopySetting() {
        return SystemInstance.get().getOptions().get(OPENEJB_LOCALCOPY, true);
    }

    protected void setDoIntraVmCopy(final boolean doIntraVmCopy) {
        this.doIntraVmCopy = doIntraVmCopy;
        setStrategy();
    }

    protected void setDoCrossClassLoaderCopy(final boolean doCrossClassLoaderCopy) {
        this.doCrossClassLoaderCopy = doCrossClassLoaderCopy;
        setStrategy();
    }

    private void setStrategy() {
        if (!doIntraVmCopy) {
            strategy = NONE;
        } else if (doCrossClassLoaderCopy) {
            strategy = CLASSLOADER_COPY;
        } else {
            strategy = COPY;
        }
    }

    /**
     * This method should be called to determine the corresponding
     * business interface class to name as the invoking interface.
     * This method should NOT be called on non-business-interface
     * methods the proxy has such as java.lang.Object or IntraVmProxy.
     *
     * @param method Method
     * @return the business (or component) interface matching this method
     */
    protected Class<?> getInvokedInterface(final Method method) {
        // Home's only have one interface ever.  We don't
        // need to verify that the method invoked is in
        // it's interface.
        final Class mainInterface = getMainInterface();
        if (interfaceType.isHome()) {
            return mainInterface;
        }
        if (interfaceType.isLocalBean()) {
            return mainInterface;
        }

        final Class declaringClass = method.getDeclaringClass();

        // If our "main" interface is or extends the method's declaring class
        // then we're good.  We know the main interface has the method being
        // invoked and it's safe to return it as the invoked interface.
        if (mainInterface != null && declaringClass.isAssignableFrom(mainInterface)) {
            return mainInterface;
        }

        // If the method being invoked isn't in the "main" interface
        // we need to find a suitable interface or throw an exception.
        for (final Class secondaryInterface : interfaces.keySet()) {
            if (declaringClass.isAssignableFrom(secondaryInterface)) {
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

    private void setMainInterface(final Class referent) {
        mainInterface = new WeakReference<>(referent);
    }

    public List<Class> getInterfaces() {
        final Set<Class> classes = interfaces.keySet();
        final List<Class> list = new ArrayList<>();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (final Class<?> clazz : classes) { // convert interfaces with current classloader -> relevant for remote interfaces
            if (clazz.isInterface() && getBeanContext().getInterfaceType(clazz) == InterfaceType.BUSINESS_REMOTE) {
                try {
                    list.add(contextClassLoader.loadClass(clazz.getName()));
                } catch (final ClassNotFoundException | NoClassDefFoundError e) {
                    list.add(clazz);
                }
            } else {
                list.add(clazz);
            }
        }
        return list;
    }

    private void setInterfaces(final List<Class> interfaces) {
        this.interfaces = new WeakHashMap<>(interfaces.size());
        for (final Class clazz : interfaces) {
            this.interfaces.put(clazz, null);
        }
    }

    protected void checkAuthorization(final Method method) throws OpenEJBException {
    }

    public void setIntraVmCopyMode(final boolean on) {
        setDoIntraVmCopy(on);
    }

    @Override
    public Object invoke(final Object proxy, Method method, Object[] args) throws Throwable {
        try {
            isValidReference(method);
        } catch (final IllegalStateException ise) {
            // bean was undeployed
            if (method.getName().equals("writeReplace")) { // session serialization, we just need to replace this
                final BeanContext beanContext = beanContextRef.get();
                if (beanContext != null) {
                    return _writeReplace(proxy);
                }
            }
            throw ise;
        }

        if (args == null) {
            args = new Object[]{};
        }

        if (method.getDeclaringClass() == Object.class) {
            final String methodName = method.getName();

            switch (methodName) {
                case "toString":
                    return toString();
                case "equals":
                    return equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
                case "hashCode":
                    return hashCode();
                default:
                    throw new UnsupportedOperationException("Unknown method: " + method);
            }
        } else if (method.getDeclaringClass() == IntraVmProxy.class) {
            final String methodName = method.getName();

            if (methodName.equals("writeReplace")) {
                return _writeReplace(proxy);
            } else {
                throw new UnsupportedOperationException("Unknown method: " + method);
            }
        } else if (method.getDeclaringClass() == BeanContext.Removable.class) {
            return _invoke(proxy, BeanContext.Removable.class, method, args);
        }

        Class interfce = getInvokedInterface(method);

        final ThreadContext callContext = ThreadContext.getThreadContext();
        final Object localClientIdentity = ClientSecurity.getIdentity();
        try {
            if (callContext == null && localClientIdentity != null) {
                final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
                securityService.associate(localClientIdentity);
            }
            if (strategy == CLASSLOADER_COPY || getBeanContext().getInterfaceType(interfce) == InterfaceType.BUSINESS_REMOTE) {

                IntraVmCopyMonitor.pre(strategy);
                final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(getBeanContext().getClassLoader());
                try {
                    args = copyArgs(args);
                    method = copyMethod(method);
                    interfce = copyObj(interfce);
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                    IntraVmCopyMonitor.post();
                }

            } else if (strategy == COPY && args != null && args.length > 0) {

                IntraVmCopyMonitor.pre(strategy);
                try {
                    args = copyArgs(args);
                } finally {
                    IntraVmCopyMonitor.post();
                }
            }

            final IntraVmCopyMonitor.State oldStrategy = strategy;
            if (getBeanContext().isAsynchronous(method) || getBeanContext().getComponentType().equals(BeanType.MANAGED)) {
                strategy = IntraVmCopyMonitor.State.NONE;
            }

            try {

                final Object returnValue = _invoke(proxy, interfce, method, args);
                return copy(strategy, returnValue);
            } catch (Throwable throwable) {
                throwable = copy(strategy, throwable);
                throw convertException(throwable, method, interfce);
            } finally {
                strategy = oldStrategy;
            }
        } finally {

            if (callContext == null && localClientIdentity != null) {
                final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
                securityService.disassociate();
            }
        }
    }

    private <T> T copy(final IntraVmCopyMonitor.State strategy, final T object) throws IOException, ClassNotFoundException {
        if (object == null || !strategy.isCopy()) {
            return object;
        }

        IntraVmCopyMonitor.pre(strategy);
        try {
            return copyObj(object);
        } finally {
            IntraVmCopyMonitor.post();
        }
    }

    public boolean isValid() {
        return !isInvalidReference;
    }

    private void isValidReference(final Method method) throws NoSuchObjectException {
        if (isInvalidReference) {
            if (interfaceType.isComponent() && interfaceType.isLocal()) {
                throw new NoSuchObjectLocalException("reference is invalid");
            } else if (interfaceType.isComponent() || Remote.class.isAssignableFrom(method.getDeclaringClass())) {
                throw new NoSuchObjectException("reference is invalid");
            } else {
                throw new NoSuchEJBException("reference is invalid for " + deploymentID);
            }
        }
        if (!(Object.class.equals(method.getDeclaringClass())
            && method.getName().equals("finalize")
            && method.getExceptionTypes().length == 1
            && Throwable.class.equals(method.getExceptionTypes()[0]))) {
            getBeanContext(); // will throw an exception if app has been undeployed.
        }
    }

    /**
     * Renamed method so it shows up with a much more understandable purpose as it
     * will be the top element in the stacktrace
     *
     * @param e        Throwable
     * @param method   Method
     * @param interfce Class
     */
    protected Throwable convertException(Throwable e, final Method method, final Class interfce) {
        final boolean rmiRemote = Remote.class.isAssignableFrom(interfce);
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
        if (e instanceof AccessException) {
            if (!rmiRemote && interfaceType.isBusiness()) {
                return new AccessLocalException(e.getMessage()).initCause(getCause(e));
            } else if (interfaceType.isLocal()) {
                return new AccessLocalException(e.getMessage()).initCause(getCause(e));
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

        for (final Class<?> type : method.getExceptionTypes()) {
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
     * @param method Method
     * @return return's the same method but loaded from the beans classloader
     */

    private Method copyMethod(final Method method) throws Exception {
        final int parameterCount = method.getParameterTypes().length;
        Class[] types = new Class[1 + parameterCount];
        types[0] = method.getDeclaringClass();
        System.arraycopy(method.getParameterTypes(), 0, types, 1, parameterCount);

        types = (Class[]) copyArgs(types);

        final Class targetClass = types[0];
        final Class[] targetParameters = new Class[parameterCount];
        System.arraycopy(types, 1, targetParameters, 0, parameterCount);
        return targetClass.getMethod(method.getName(), targetParameters);
    }

    protected Throwable getCause(final Throwable e) {
        if (e != null && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }

    public String toString() {
        String name = null;
        try {
            name = getProxyInfo().getInterface().getName();
        } catch (final Exception e) {
            //Ignore
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

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!BaseEjbProxyHandler.class.isInstance(obj)) {
            final Class<?> aClass = obj.getClass();
            if (Proxy.isProxyClass(aClass)) {
                obj = Proxy.getInvocationHandler(obj);
            } else if (LocalBeanProxyFactory.isProxy(aClass)) {
                obj = LocalBeanProxyFactory.getInvocationHandler(obj);
            } else {
                return false;
            }
        }
        return equalHandler(BaseEjbProxyHandler.class.cast(obj));
    }

    protected boolean equalHandler(final BaseEjbProxyHandler other) {
        return (Objects.equals(primaryKey, other.primaryKey))
            && deploymentID.equals(other.deploymentID)
            && getMainInterface().equals(other.getMainInterface());
    }

    protected abstract Object _invoke(Object proxy, Class interfce, Method method, Object[] args) throws Throwable;

    protected Object[] copyArgs(final Object[] objects) throws IOException, ClassNotFoundException {
        if (objects == null) {
            return objects;
        }
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
    protected <T> T copyObj(final T object) throws IOException, ClassNotFoundException {
        // Check for primitive and other known class types that are immutable.  If detected
        // we can safely return them.
        if (object == null) {
            return null;
        }
        final Class ooc = object.getClass();
        if (ooc == int.class ||
            ooc == String.class ||
            ooc == long.class ||
            ooc == boolean.class ||
            ooc == byte.class ||
            ooc == float.class ||
            ooc == double.class ||
            ooc == short.class ||
            ooc == Long.class ||
            ooc == Boolean.class ||
            ooc == Byte.class ||
            ooc == Character.class ||
            ooc == Float.class ||
            ooc == Double.class ||
            ooc == Short.class ||
            ooc == BigDecimal.class) {
            return object;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        try {
            final ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.close();
        } catch (final NotSerializableException e) {
            throw (IOException) new NotSerializableException(e.getMessage() +
                " : The EJB specification restricts remote interfaces to only serializable data types.  This can be disabled for in-vm use with the " +
                OPENEJB_LOCALCOPY +
                "=false system property.").initCause(e);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream in = new EjbObjectInputStream(bais);
        final Object obj = in.readObject();
        return (T) obj;
    }

    public void invalidateReference() {
        this.container = null;
        this.setBeanContext(null);
        this.isInvalidReference = true;
    }

    protected void invalidateAllHandlers(final Object key) {
        final HashSet<BaseEjbProxyHandler> set = (HashSet) getLiveHandleRegistry().remove(key);
        if (set == null) {
            return;
        }

        final ReentrantLock l = lock;
        l.lock();

        try {
            for (final BaseEjbProxyHandler handler : set) {
                handler.invalidateReference();
            }
        } finally {
            l.unlock();
        }
    }

    protected abstract Object _writeReplace(Object proxy) throws ObjectStreamException;

    protected void registerHandler(final Object key, final BaseEjbProxyHandler handler) {
        Set set = (Set) getLiveHandleRegistry().get(key);
        if (set == null) {
            set = new HashSet();
            final Object existing = getLiveHandleRegistry().putIfAbsent(key, set);
            if (existing != null) {
                set = Set.class.cast(existing);
            }
        }
        final ReentrantLock l = lock;
        l.lock();
        try {
            set.add(handler);
        } finally {
            l.unlock();
        }
    }

    public abstract ProxyInfo getProxyInfo();

    public BeanContext getBeanContext() {
        final BeanContext beanContext = beanContextRef.get();
        if (beanContext == null || beanContext.isDestroyed()) {
            invalidateReference();
            throw new IllegalStateException("Bean '" + deploymentID + "' has been undeployed.");
        }
        return beanContext;
    }

    public void setBeanContext(final BeanContext beanContext) {
        this.beanContextRef = new WeakReference<>(beanContext);
    }

    public ConcurrentMap getLiveHandleRegistry() {
        final BeanContext beanContext = getBeanContext();

        final ThreadContext tc = ThreadContext.getThreadContext();
        if (tc != null && tc.getBeanContext() != beanContext /* parent bean */ && tc.getCurrentOperation() == Operation.BUSINESS) {
            ProxyRegistry registry = tc.get(ProxyRegistry.class);
            if (registry == null) {
                registry = new ProxyRegistry();
                tc.set(ProxyRegistry.class, registry);
            }
            return registry.liveHandleRegistry;
        } else { // use the tx if there
            final SystemInstance systemInstance = SystemInstance.get();
            final TransactionManager txMgr = systemInstance.getComponent(TransactionManager.class);
            try {
                final Transaction tx = txMgr.getTransaction();
                if (tx != null && tx.getStatus() == Status.STATUS_ACTIVE) {
                    final TransactionSynchronizationRegistry registry = systemInstance.getComponent(TransactionSynchronizationRegistry.class);
                    final String resourceKey = ProxyRegistry.class.getName();
                    ConcurrentMap map = ConcurrentMap.class.cast(registry.getResource(resourceKey));
                    if (map == null) {
                        map = new ConcurrentHashMap();
                        registry.putResource(resourceKey, map);
                        try {
                            final ConcurrentMap tmp = map;
                            tx.registerSynchronization(new Synchronization() {
                                @Override
                                public void beforeCompletion() {
                                    // no-op
                                }

                                @Override
                                public void afterCompletion(final int status) {
                                    tmp.clear();
                                }
                            });
                        } catch (final RollbackException e) { // not really possible since we check the status
                            // let it go to default
                        }
                    }
                    return map;
                }
            } catch (final SystemException e) {
                // let it go to default
            }

            // back to default but it doesnt release the memory
            ProxyRegistry proxyRegistry = beanContext.get(ProxyRegistry.class);
            if (proxyRegistry == null) {
                proxyRegistry = new ProxyRegistry();
                beanContext.set(ProxyRegistry.class, proxyRegistry);
            }
            return proxyRegistry.liveHandleRegistry;
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        out.writeObject(getInterfaces());
        out.writeObject(getMainInterface());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        setBeanContext(containerSystem.getBeanContext(deploymentID));
        container = (RpcContainer) getBeanContext().getContainer();

        if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            setDoCrossClassLoaderCopy(true);
        }

        setInterfaces((List<Class>) in.readObject());
        setMainInterface((Class) in.readObject());
    }

    private static class ProxyRegistry {

        protected final ConcurrentMap liveHandleRegistry = new ConcurrentHashMap();
    }

}
