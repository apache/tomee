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

import org.apache.openejb.client.proxy.InvocationHandler;

import javax.ejb.AccessLocalException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public abstract class EJBInvocationHandler implements InvocationHandler, Serializable {

    private static final ReentrantLock lock = new ReentrantLock();

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

    protected transient JNDIContext.AuthenticationInfo authenticationInfo;

    /**
     * The EJB spec requires that a different set of exceptions
     * be thrown for the legacy EJBObject and EJBHome interfaces
     * than newer @Remote interfaces
     */
    protected final boolean remote;

    public EJBInvocationHandler() {
        remote = false;
    }

    public EJBInvocationHandler(final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        this.ejb = ejb;
        this.server = server;
        this.client = client;
        this.authenticationInfo = auth;
        final Class remoteInterface = ejb.getRemoteInterfaceClass();
        remote = remoteInterface != null && (EJBObject.class.isAssignableFrom(remoteInterface) || EJBHome.class.isAssignableFrom(remoteInterface));
    }

    public EJBInvocationHandler(final EJBMetaDataImpl ejb,
                                final ServerMetaData server,
                                final ClientMetaData client,
                                final Object primaryKey,
                                final JNDIContext.AuthenticationInfo auth) {
        this(ejb, server, client, auth);
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

    @SuppressWarnings("unchecked")
    protected static Method getMethod(final Class c, final String method, final Class... params) {
        try {
            return c.getMethod(method, params);
        } catch (NoSuchMethodException nse) {
            throw new IllegalStateException("Cannot find method: " + c.getName() + "." + method, nse);
        } catch (java.lang.ExceptionInInitializerError eiie) {
            throw new IllegalStateException("Invalid parameters for method: " + c.getName() + "." + method + " : " + Arrays.toString(params), eiie);
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object... args) throws Throwable {
        if (isInvalidReference.get()) {
            if (remote || java.rmi.Remote.class.isAssignableFrom(method.getDeclaringClass())) {
                throw new NoSuchObjectException("reference is invalid");
            } else {
                throw new NoSuchEJBException("reference is invalid");
            }
        }

        return _invoke(proxy, method, args);
    }

    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;

    protected EJBResponse request(final EJBRequest req) throws Exception {
        req.setClientIdentity(getClientIdentity());

        req.setServerHash(server.buildHash());

        final EJBResponse response = new EJBResponse();
        Client.request(req, response, server);
        if (null != response.getServer()) {
            server.merge(response.getServer());
        }
        return response;
    }

    protected EJBResponse request(final EJBRequest req, final EJBResponse res) throws Exception {
        req.setClientIdentity(getClientIdentity());

        req.setServerHash(server.buildHash());

        Client.request(req, res, server);
        if (null != res.getServer()) {
            server.merge(res.getServer());
        }
        return res;
    }

    protected Object getClientIdentity() {
        if (client != null) {
            final Object identity = client.getClientIdentity();
            if (identity != null) {
                return identity;
            }
        }

        return ClientSecurity.getIdentity();
    }

    protected void invalidateReference() {
        this.isInvalidReference.set(true);
    }

    protected static void invalidateAllHandlers(final Object key) {

        final Set<WeakReference<EJBInvocationHandler>> set = liveHandleRegistry.remove(key);
        if (set == null) {
            return;
        }

        final ReentrantLock l = lock;
        l.lock();

        try {
            for (final WeakReference<EJBInvocationHandler> ref : set) {
                final EJBInvocationHandler handler = ref.get();
                if (handler != null) {
                    handler.invalidateReference();
                }
            }
            set.clear();
        } finally {
            l.unlock();
        }
    }

    protected static void registerHandler(final Object key, final EJBInvocationHandler handler) {
    	final ReentrantLock l = lock;
    	l.lock();

    	try {
        	// this map lookup must be synchronized even though it is a ConcurrentHashMap to avoid race condition with the cleanup below
            final Set<WeakReference<EJBInvocationHandler>> set = liveHandleRegistry.computeIfAbsent(key, k -> new HashSet<>());
            set.add(new WeakReference<>(handler));

    		// loop through and remove old references that have been garbage collected
    		for (Iterator<Map.Entry<Object,Set<WeakReference<EJBInvocationHandler>>>> i = liveHandleRegistry.entrySet().iterator(); i.hasNext(); ) {
                final Map.Entry<Object,Set<WeakReference<EJBInvocationHandler>>> entry = i.next();
                final Set<WeakReference<EJBInvocationHandler>> s = entry.getValue();
    			for (Iterator<WeakReference<EJBInvocationHandler>> j = s.iterator(); j.hasNext(); ) {
                    final WeakReference<EJBInvocationHandler> ref = j.next();
    				if (ref.get() == null) {
    					j.remove(); // clean up old WeakReference
    				}
    			}
    			if (s.isEmpty()) {
    				i.remove(); // no more handlers for this primary key, remove map entry
    			}
    		}
    	} finally {
    		l.unlock();
    	}
    }

    /**
     * Renamed method, so it shows up with a much more understandable purpose as it
     * will be the top element in the stacktrace
     *
     * @param e      Throwable
     * @param method Method
     */
    protected Throwable convertException(final Throwable e, final Method method) {
        if (!remote && e instanceof RemoteException) {
            if (e instanceof TransactionRequiredException) {
                return new TransactionRequiredLocalException(e.getMessage()).initCause(getCause(e));
            }
            if (e instanceof TransactionRolledbackException) {
                return new TransactionRolledbackLocalException(e.getMessage()).initCause(getCause(e));
            }

            /*
             * If a client attempts to invoke a method on a removed bean's business interface,
             * we must throw a javax.ejb.NoSuchEJBException. If the business interface is a
             * remote business interface that extends java.rmi.Remote, the
             * java.rmi.NoSuchObjectException is thrown to the client instead.
             * See EJB 3.0, section 4.4
             */
            if (e instanceof NoSuchObjectException) {
                if (java.rmi.Remote.class.isAssignableFrom(method.getDeclaringClass())) {
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

    protected static Throwable getCause(final Throwable e) {
        if (e != null && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }
}