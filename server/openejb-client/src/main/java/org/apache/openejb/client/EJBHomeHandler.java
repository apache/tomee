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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.Handle;

import org.apache.openejb.client.proxy.ProxyManager;

public abstract class EJBHomeHandler extends EJBInvocationHandler implements Externalizable {

    protected static final Method GETEJBMETADATA = getMethod(EJBHome.class, "getEJBMetaData", null);
    protected static final Method GETHOMEHANDLE = getMethod(EJBHome.class, "getHomeHandle", null);
    protected static final Method REMOVE_W_KEY = getMethod(EJBHome.class, "remove", new Class []{Object.class});
    protected static final Method REMOVE_W_HAND = getMethod(EJBHome.class, "remove", new Class []{Handle.class});
    protected static final Method GETHANDLER = getMethod(EJBHomeProxy.class, "getEJBHomeHandler", null);

    public EJBHomeHandler() {
    }

    public EJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public static EJBHomeHandler createEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {

        switch (ejb.type) {
            case EJBMetaDataImpl.BMP_ENTITY:
            case EJBMetaDataImpl.CMP_ENTITY:

                return new EntityEJBHomeHandler(ejb, server, client);

            case EJBMetaDataImpl.STATEFUL:

                return new StatefulEJBHomeHandler(ejb, server, client);

            case EJBMetaDataImpl.STATELESS:

                return new StatelessEJBHomeHandler(ejb, server, client);
        }
        return null;

    }

//    protected abstract EJBObjectHandler newEJBObjectHandler();

    public EJBHomeProxy createEJBHomeProxy() {
        try {
            Class[] interfaces = new Class[]{EJBHomeProxy.class, ejb.homeClass};
            return (EJBHomeProxy) ProxyManager.newProxyInstance(interfaces, this);
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        }
        return null;
    }

    protected Object _invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();

        try {

            if (method.getDeclaringClass() == Object.class) {
                if (method.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (method.equals(EQUALS)) {

                    return Boolean.FALSE;

                } else if (method.equals(HASHCODE)) {
                    return new Integer(this.hashCode());

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

        } catch (SystemException se) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException", se.getCause());
        } catch (SystemError se) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException", se.getCause());
        }

    }

    public Object homeMethod(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest(RequestMethodConstants.EJB_HOME_METHOD);

        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);

        EJBResponse res = request(req);

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

    protected Object create(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest(RequestMethodConstants.EJB_HOME_CREATE);

        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:

                Object primKey = res.getResult();
                EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primKey);
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

    protected Object getEJBMetaData(Method method, Object[] args, Object proxy) throws Throwable {
        return ejb;
    }

    protected Object getHomeHandle(Method method, Object[] args, Object proxy) throws Throwable {

        return new EJBHomeHandle((EJBHomeProxy) proxy);
    }

    protected abstract Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }

}

