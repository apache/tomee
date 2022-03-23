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

import org.apache.openejb.client.proxy.ProxyManager;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.Handle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("NullArgumentToVariableArgMethod")
public abstract class EJBHomeHandler extends EJBInvocationHandler implements Externalizable {

    private static final long serialVersionUID = 4212515330775330214L;
    protected static final Method GETEJBMETADATA = getMethod(EJBHome.class, "getEJBMetaData", null);
    protected static final Method GETHOMEHANDLE = getMethod(EJBHome.class, "getHomeHandle", null);
    @SuppressWarnings("RedundantArrayCreation")
    protected static final Method REMOVE_W_KEY = getMethod(EJBHome.class, "remove", new Class[]{Object.class});
    @SuppressWarnings("RedundantArrayCreation")
    protected static final Method REMOVE_W_HAND = getMethod(EJBHome.class, "remove", new Class[]{Handle.class});
    protected static final Method GETHANDLER = getMethod(EJBHomeProxy.class, "getEJBHomeHandler", null);
    protected ThreadPoolExecutor executor;

    public EJBHomeHandler() {
    }

    public EJBHomeHandler(final ThreadPoolExecutor executor, final EJBMetaDataImpl ejb, final ServerMetaData server, final ClientMetaData client, final JNDIContext.AuthenticationInfo auth) {
        super(ejb, server, client, auth);
        this.executor = executor;
    }

    public static EJBHomeHandler createEJBHomeHandler(final ThreadPoolExecutor executor,
                                                      final EJBMetaDataImpl ejb,
                                                      final ServerMetaData server,
                                                      final ClientMetaData client,
                                                      final JNDIContext.AuthenticationInfo auth) {
        switch (ejb.type) {
            case EJBMetaDataImpl.BMP_ENTITY:
            case EJBMetaDataImpl.CMP_ENTITY:

                return new EntityEJBHomeHandler(executor, ejb, server, client, auth);

            case EJBMetaDataImpl.STATEFUL:

                return new StatefulEJBHomeHandler(executor, ejb, server, client, auth);

            case EJBMetaDataImpl.STATELESS:

                return new StatelessEJBHomeHandler(executor, ejb, server, client, auth);

            case EJBMetaDataImpl.SINGLETON:

                return new SingletonEJBHomeHandler(executor, ejb, server, client, auth);
        }

        throw new IllegalStateException("Uknown bean type code '" + ejb.type + "' : " + ejb.toString());

    }

    //    protected abstract EJBObjectHandler newEJBObjectHandler();

    public EJBHomeProxy createEJBHomeProxy() {
        try {
            // Interface class must be listed first otherwise the proxy code will select
            // the openejb system class loader for proxy creation instead of the
            // application class loader
            final Class[] interfaces = new Class[]{ejb.homeClass, EJBHomeProxy.class};
            return (EJBHomeProxy) ProxyManager.newProxyInstance(interfaces, this);
        } catch (IllegalAccessException e) {
            throw new ClientRuntimeException("Unable to create proxy for " + ejb.homeClass, e);
        }
    }

    @Override
    protected Object _invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final String methodName = method.getName();

        try {

            if (method.getDeclaringClass() == Object.class) {
                if (method.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (method.equals(EQUALS)) {

                    return Boolean.FALSE;

                } else if (method.equals(HASHCODE)) {
                    return this.hashCode();

                } else {
                    throw new UnsupportedOperationException("Unkown method: " + method);
                }
            } else if (method.getDeclaringClass() == EJBHomeProxy.class) {
                if (method.equals(GETHANDLER)) {
                    return this;
                } else if (methodName.equals("writeReplace")) {
                    return new EJBHomeProxyHandle(this);
                } else if (methodName.equals("readResolve")) {

                    throw new UnsupportedOperationException("Unkown method: " + method);

                } else {
                    throw new UnsupportedOperationException("Unkown method: " + method);
                }
            }
            /*-------------------------------------------------------*/

            /*-- CREATE ------------- <HomeInterface>.create(<x>) ---*/
            if (methodName.startsWith("create")) {
                return create(method, args, proxy);

                /*-- FIND X --------------- <HomeInterface>.find<x>() ---*/
            } else if (methodName.startsWith("find")) {
                return findX(method, args, proxy);

                /*-- GET EJB METADATA ------ EJBHome.getEJBMetaData() ---*/

            } else if (method.equals(GETEJBMETADATA)) {
                return getEJBMetaData(method, args, proxy);

                /*-- GET HOME HANDLE -------- EJBHome.getHomeHandle() ---*/

            } else if (method.equals(GETHOMEHANDLE)) {
                return getHomeHandle(method, args, proxy);

                /*-- REMOVE ------------------------ EJBHome.remove() ---*/

            } else if (method.equals(REMOVE_W_HAND)) {
                return removeWithHandle(method, args, proxy);

            } else if (method.equals(REMOVE_W_KEY)) {
                return removeByPrimaryKey(method, args, proxy);

                /*-- UNKOWN ---------------------------------------------*/
            } else {
                return homeMethod(method, args, proxy);
            }

        } catch (SystemException e) {
            invalidateReference();
            throw convertException(getCause(e), method);
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (ApplicationException ae) {
            throw convertException(getCause(ae), method);
            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (SystemError se) {
            invalidateReference();
            if (remote) {
                throw new RemoteException("Container has suffered a SystemException", getCause(se));
            } else {
                throw new EJBException("Container has suffered a SystemException").initCause(getCause(se));
            }
        } catch (Throwable oe) {
            if (remote) {
                throw new RemoteException("Unknown Client Exception", oe);
            } else {
                throw new EJBException("Unknown Client Exception").initCause(oe);
            }
        }

    }

    public Object homeMethod(final Method method, final Object[] args, final Object proxy) throws Throwable {
        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_METHOD, ejb, method, args, null, client.getSerializer());

        final EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:

                return res.getResult();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    /*-------------------------------------------------*/
    /*  Home interface methods                         */
    /*-------------------------------------------------*/

    protected Object create(final Method method, final Object[] args, final Object proxy) throws Throwable {
        final EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_CREATE, ejb, method, args, null, client.getSerializer());

        final EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:

                final Object primKey = res.getResult();
                final EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(executor, ejb, server, client, primKey, authenticationInfo);
                handler.setEJBHomeProxy((EJBHomeProxy) proxy);

                return handler.createEJBObjectProxy();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    protected abstract Object findX(Method method, Object[] args, Object proxy) throws Throwable;

    /*-------------------------------------------------*/
    /*  EJBHome methods                                */
    /*-------------------------------------------------*/

    protected Object getEJBMetaData(final Method method, final Object[] args, final Object proxy) throws Throwable {
        return ejb;
    }

    protected Object getHomeHandle(final Method method, final Object[] args, final Object proxy) throws Throwable {

        return new EJBHomeHandle((EJBHomeProxy) proxy);
    }

    protected abstract Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
    }

}

