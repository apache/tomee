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
package org.apache.openejb.client;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.openejb.client.proxy.InvocationHandler;

import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.ejb.AccessLocalException;
import javax.ejb.EJBException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.NoSuchEJBException;
import javax.ejb.EJBTransactionRolledbackException;

public abstract class EJBInvocationHandler implements InvocationHandler, Serializable {

    protected static final Method EQUALS = getMethod(Object.class, "equals", Object.class);
    protected static final Method HASHCODE = getMethod(Object.class, "hashCode");
    protected static final Method TOSTRING = getMethod(Object.class, "toString");

    protected static final ConcurrentMap<Object, Set<WeakReference<EJBInvocationHandler>>> liveHandleRegistry = new ConcurrentHashMap<Object, Set<WeakReference<EJBInvocationHandler>>>();

    protected transient boolean inProxyMap = false;

    protected transient AtomicBoolean isInvalidReference = new AtomicBoolean(false);

    protected transient EJBRequest request;

    protected transient EJBMetaDataImpl ejb;
    protected transient ServerMetaData server;
    protected transient ClientMetaData client;

    protected transient Object primaryKey;
    protected final boolean remote;

    public EJBInvocationHandler() {
        remote = false;
    }

    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        this.ejb = ejb;
        this.server = server;
        this.client = client;
        Class remoteInterface = ejb.getRemoteInterfaceClass();
        remote = remoteInterface != null && (EJBObject.class.isAssignableFrom(remoteInterface) || EJBHome.class.isAssignableFrom(remoteInterface));
    }

    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        this(ejb, server, client);
        this.primaryKey = primaryKey;
    }

    public EJBMetaDataImpl getEjb() {
        return ejb;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public ClientMetaData getClient() {
        return client;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    protected static Method getMethod(Class c, String method, Class... params) {
        try {
            return c.getMethod(method, params);
        } catch (NoSuchMethodException nse) {
            throw new IllegalStateException("Cannot find method: "+c.getName()+"."+method, nse);

        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isInvalidReference.get()) {
            if (remote || java.rmi.Remote.class.isAssignableFrom(method.getDeclaringClass())){
                throw new NoSuchObjectException("reference is invalid");
            } else {
                throw new NoSuchEJBException("reference is invalid");
            }
        }

        Object returnObj = null;
        returnObj = _invoke(proxy, method, args);
        return returnObj;
    }

    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;

    protected EJBResponse request(EJBRequest req) throws Exception {
        req.setClientIdentity(getClientIdentity());

        req.setServerHash(server.buildHash());

        EJBResponse response = new EJBResponse();
        Client.request(req, response, server);
        if (null != response.getServer()) {
            server.merge(response.getServer());
        }
        return response;
    }

    protected Object getClientIdentity() {
        if (client != null) {
            Object identity = client.getClientIdentity();
            if (identity != null) {
                return identity;
            }
        }

        return ClientSecurity.getIdentity();
    }

    protected void invalidateReference() {
        this.isInvalidReference.set(true);
    }

    protected static void invalidateAllHandlers(Object key) {

        Set<WeakReference<EJBInvocationHandler>> set = liveHandleRegistry.remove(key);
        if (set == null) return;

        synchronized (set) {
            for (WeakReference<EJBInvocationHandler> ref : set) {
                EJBInvocationHandler handler = ref.get();
                if (handler != null) {
                    handler.invalidateReference();
                }
            }
            set.clear();
        }
    }

    protected static void registerHandler(Object key, EJBInvocationHandler handler) {
        Set<WeakReference<EJBInvocationHandler>> set = liveHandleRegistry.get(key);

        if (set == null) {
            set = new HashSet<WeakReference<EJBInvocationHandler>>();
            Set<WeakReference<EJBInvocationHandler>> current = liveHandleRegistry.putIfAbsent(key, set);
            // someone else added the set
            if (current != null) set = current;
        }

        synchronized (set) {
            set.add(new WeakReference<EJBInvocationHandler>(handler));
        }
    }

    /**
     * Renamed method so it shows up with a much more understandable purpose as it
     * will be the top element in the stacktrace
     * @param e
     * @param method
     */
    protected Throwable convertException(Throwable e, Method method) {
        if (!remote && e instanceof RemoteException) {
            if (e instanceof TransactionRequiredException) {
                return new TransactionRequiredLocalException(e.getMessage()).initCause(getCause(e));
            }
            if (e instanceof TransactionRolledbackException) {
                return new TransactionRolledbackLocalException(e.getMessage()).initCause(getCause(e));
            }

            /**
             * If a client attempts to invoke a method on a removed bean's business interface,
             * we must throw a javax.ejb.NoSuchEJBException. If the business interface is a
             * remote business interface that extends java.rmi.Remote, the
             * java.rmi.NoSuchObjectException is thrown to the client instead.
             * See EJB 3.0, section 4.4
             */
            if (e instanceof NoSuchObjectException) {
                if (java.rmi.Remote.class.isAssignableFrom(method.getDeclaringClass())){
                    return e;
                } else {
                    return new NoSuchEJBException(e.getMessage()).initCause(getCause(e));
                }
            }
            if (e instanceof AccessException) {
                return new AccessLocalException(e.getMessage()).initCause(getCause(e));
            }
            return new EJBException(e.getMessage()).initCause(getCause(e));
        }

        if (remote && e instanceof EJBAccessException) {
            if (e.getCause() instanceof Exception) {
                return new AccessException(e.getMessage(), (Exception) e.getCause());
            } else {
                return new AccessException(e.getMessage());
            }
        }
        if (!remote && e instanceof EJBTransactionRolledbackException) {
            return new TransactionRolledbackLocalException(e.getMessage()).initCause(getCause(e));
        }
        return e;
    }

    protected static Throwable getCause(Throwable e) {
        if (e != null && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }
}