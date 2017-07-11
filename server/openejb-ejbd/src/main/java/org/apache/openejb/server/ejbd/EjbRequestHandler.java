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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.client.EJBHomeProxyHandle;
import org.apache.openejb.client.EJBObjectProxyHandle;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.EJBResponse;
import org.apache.openejb.client.JNDIContext;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.Response;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.ThrowableArtifact;
import org.apache.openejb.client.serializer.EJBDSerializer;
import org.apache.openejb.client.serializer.SerializationWrapper;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.security.auth.login.LoginException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class EjbRequestHandler extends RequestHandler {

    public static final ServerSideResolver SERVER_SIDE_RESOLVER = new ServerSideResolver();

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("ejb"), "org.apache.openejb.server.util.resources");

    private final ClusterableRequestHandler clusterableRequestHandler;

    private final Map<String, AtomicBoolean> asynchronousInvocationCancelMap = new ConcurrentHashMap<String, AtomicBoolean>();

    protected EjbRequestHandler(final EjbDaemon daemon) {
        super(daemon);
        clusterableRequestHandler = newClusterableRequestHandler();
    }

    protected BasicClusterableRequestHandler newClusterableRequestHandler() {
        return new BasicClusterableRequestHandler();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getName() {
        return "EJB";
    }

    @Override
    public Response processRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {

        // Setup the client proxy replacement to replace
        // the proxies with the IntraVM proxy implementations
        EJBHomeProxyHandle.resolver.set(SERVER_SIDE_RESOLVER);
        EJBObjectProxyHandle.resolver.set(SERVER_SIDE_RESOLVER);

        final EJBRequest req = new EJBRequest();
        req.setMetaData(metaData);
        byte version = req.getVersion();

        final EJBResponse res = new EJBResponse();
        res.setMetaData(metaData);
        res.start(EJBResponse.Time.TOTAL);
        res.setRequest(req);

        try {
            req.readExternal(in);
        } catch (final Throwable t) {
            return setResponseError(res, version, t, "Bad request");
        }

        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        boolean failed = false;
        final CallContext call;
        Object oldClientIdentity = null;


        try {
            try {
                final Object clientIdentity = req.getClientIdentity();
                if (clientIdentity != null) {//noinspection unchecked
                    if (securityService.getCallerPrincipal() != null) {
                        oldClientIdentity = securityService.disassociate();
                    }

                    securityService.associate(clientIdentity);
                }
            } catch (final LoginException t) {
                failed = true;
                return setResponseError(res, version, t, "Client identity is not valid - " + req);
            }

            final BeanContext di;

            try {
                di = this.daemon.getDeployment(req);
            } catch (final RemoteException e) {
                failed = true;
                return setResponseError(res, version, e, "No such deployment");
            } catch (final Throwable t) {
                failed = true;
                return setResponseError(res, version, t, "Unkown error occured while retrieving deployment: " + req);
            }

            try {

                //Need to set this for deserialization of the body - Will always be reset by EjbDaemon
                final ClassLoader classLoader = di.getBeanClass().getClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);

                res.start(EJBResponse.Time.DESERIALIZATION);

                req.getBody().readExternal(in);

                //Client version retrieved from body
                version = req.getVersion();

                res.stop(EJBResponse.Time.DESERIALIZATION);
            } catch (final Throwable t) {
                failed = true;
                return setResponseError(res, version, t, "Error caught during request body deserialization: " + req);
            }

            try {
                call = CallContext.getCallContext();
                call.setEJBRequest(req);
                call.setBeanContext(di);
            } catch (final Throwable t) {
                failed = true;
                return setResponseError(res, version, t, "Unable to set the thread call context for this request: " + req);
            }

        } finally {
            if (failed) {
                securityService.disassociate();

                if (oldClientIdentity != null) {
                    securityService.associate(oldClientIdentity);
                }
            }
        }

        res.start(EJBResponse.Time.CONTAINER);

        Object securityToken = null;
        try {
            final JNDIContext.AuthenticationInfo authentication = req.getBody().getAuthentication();
            if (authentication != null) {
                try {
                    securityToken = securityService.login(authentication.getRealm(), authentication.getUser(), new String(authentication.getPassword()));
                } catch (final Throwable t) {
                    res.setResponse(req.getVersion(), ResponseCodes.AUTH_DENIED, t);
                }
            }

            if (res.getResponseCode() != ResponseCodes.AUTH_DENIED) {
                switch (req.getRequestMethod()) {
                    // Remote interface methods
                    case EJB_OBJECT_BUSINESS_METHOD:
                        doEjbObject_BUSINESS_METHOD(req, res);
                        updateServer(req, res);
                        break;

                    // Home interface methods
                    case EJB_HOME_CREATE:
                        doEjbHome_CREATE(req, res);
                        updateServer(req, res);
                        break;

                    // Home interface methods
                    case EJB_HOME_METHOD:
                        doEjbHome_METHOD(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_HOME_FIND:
                        doEjbHome_FIND(req, res);
                        updateServer(req, res);
                        break;

                    // javax.ejb.EJBObject methods
                    case EJB_OBJECT_GET_EJB_HOME:
                        doEjbObject_GET_EJB_HOME(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_OBJECT_GET_HANDLE:
                        doEjbObject_GET_HANDLE(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_OBJECT_GET_PRIMARY_KEY:
                        doEjbObject_GET_PRIMARY_KEY(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_OBJECT_IS_IDENTICAL:
                        doEjbObject_IS_IDENTICAL(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_OBJECT_REMOVE:
                        doEjbObject_REMOVE(req, res);
                        break;

                    // javax.ejb.EJBHome methods
                    case EJB_HOME_GET_EJB_META_DATA:
                        doEjbHome_GET_EJB_META_DATA(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_HOME_GET_HOME_HANDLE:
                        doEjbHome_GET_HOME_HANDLE(req, res);
                        updateServer(req, res);
                        break;

                    case EJB_HOME_REMOVE_BY_HANDLE:
                        doEjbHome_REMOVE_BY_HANDLE(req, res);
                        break;

                    case EJB_HOME_REMOVE_BY_PKEY:
                        doEjbHome_REMOVE_BY_PKEY(req, res);
                        break;

                    case FUTURE_CANCEL:
                        doFUTURE_CANCEL_METHOD(req, res);
                        break;

                    default:
                        throw new org.apache.openejb.SystemException("Unexpected request method: " + req.getRequestMethod());
                }
            }

        } catch (final org.apache.openejb.InvalidateReferenceException e) {
            res.setResponse(version, ResponseCodes.EJB_SYS_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (final org.apache.openejb.ApplicationException e) {
            res.setResponse(version, ResponseCodes.EJB_APP_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (final org.apache.openejb.SystemException e) {
            res.setResponse(version, ResponseCodes.EJB_ERROR, new ThrowableArtifact(e.getRootCause()));
            logger.error("System error in container for request: " + req, e);
        } catch (final Throwable t) {

            return setResponseError(res, version, t, "Unknown error in container");

        } finally {
            if (securityToken != null) {
                try {
                    //noinspection unchecked
                    securityService.logout(securityToken);

                    if (oldClientIdentity != null) {
                        securityService.associate(oldClientIdentity);
                    }
                } catch (final LoginException e) {
                    // no-op
                }
            }

            try {
                res.stop(EJBResponse.Time.CONTAINER);
            } catch (final Throwable e) {
                //Ignore
            }

            if (logger.isDebugEnabled()) {
                //The req and res toString overrides are volatile
                try {
                    logger.debug("EJB REQUEST: " + req + " -- RESPONSE: " + res);
                } catch (final Throwable t) {
                    //Ignore
                }
            }
        }

        return res;
    }

    @Override
    public void processResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        if (EJBResponse.class.isInstance(response)) {

            final EJBResponse res = (EJBResponse) response;

            try {
                res.setMetaData(metaData);
                res.writeExternal(out);
            } catch (final Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to write EjbResponse", t);
                } else if (logger.isInfoEnabled()) {
                    logger.info("Failed to write EjbResponse - Debug for stacktrace: " + t);
                }
            } finally {
                try {
                    SystemInstance.get().getComponent(SecurityService.class).disassociate();
                } catch (final Throwable t) {
                    logger.warning("Failed to disassociate security", t);
                }

                final CallContext call = CallContext.getCallContext();
                if (null != call) {
                    call.reset();
                }

                EJBHomeProxyHandle.resolver.set(null);
                EJBObjectProxyHandle.resolver.set(null);
            }
        } else {
            logger.error("EjbRequestHandler cannot process an instance of: " + response.getClass().getName());
        }
    }

    protected void updateServer(final EJBRequest req, final EJBResponse res) {
        final CallContext callContext = CallContext.getCallContext();
        final BeanContext beanContext = callContext.getBeanContext();
        clusterableRequestHandler.updateServer(beanContext, req, res);
    }

    protected void doFUTURE_CANCEL_METHOD(final EJBRequest req, final EJBResponse res) throws Exception {
        final AtomicBoolean invocationCancelTag = asynchronousInvocationCancelMap.get(req.getBody().getRequestId());
        if (invocationCancelTag == null) {
            //TODO ?
        } else {
            invocationCancelTag.set((Boolean) req.getBody().getMethodParameters()[0]);
            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, null);
        }
    }

    protected void doEjbObject_BUSINESS_METHOD(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = call.getBeanContext();
        final boolean asynchronous = beanContext.isAsynchronous(req.getMethodInstance());
        try {
            if (asynchronous) {
                final AtomicBoolean invocationCancelTag = new AtomicBoolean(false);
                ThreadContext.initAsynchronousCancelled(invocationCancelTag);
                asynchronousInvocationCancelMap.put(req.getBody().getRequestId(), invocationCancelTag);
            }
            final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

            //            Object result = c.invoke(req.getDeploymentId(),
            //                    req.getInterfaceClass(), req.getMethodInstance(),
            //                    req.getMethodParameters(),
            //                    req.getPrimaryKey()
            //            );

            final EJBDSerializer serializer = daemon.getSerializer();
            if (serializer != null) {
                req.setSerializer(serializer);
            }

            Object result = c.invoke(
                req.getDeploymentId(),
                InterfaceType.EJB_OBJECT,
                req.getInterfaceClass(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey());

            //Pass the internal value to the remote client, as AsyncResult is not serializable
            if (result != null && asynchronous) {
                result = ((Future) result).get();
            }

            final Object realResult;
            if (serializer != null && result != null) {
                realResult = new SerializationWrapper(serializer.serialize(result), result.getClass().getName());
            } else {
                realResult = result;
            }
            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, realResult);
        } finally {
            if (asynchronous) {
                ThreadContext.removeAsynchronousCancelled();
                asynchronousInvocationCancelMap.remove(req.getBody().getRequestId());
            }
        }
    }

    protected void doEjbHome_METHOD(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        final Object result = c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_HOME,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, result);
    }

    protected void doEjbHome_CREATE(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_HOME,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        if (result instanceof ProxyInfo) {
            final ProxyInfo info = (ProxyInfo) result;
            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, info.getPrimaryKey());
        } else {

            result = new RemoteException("The bean is not EJB compliant.  The bean should be created or and exception should be thrown.");
            logger.error(req + "The bean is not EJB compliant.  The bean should be created or and exception should be thrown.");
            res.setResponse(req.getVersion(), ResponseCodes.EJB_SYS_EXCEPTION, new ThrowableArtifact((Throwable) result));
        }
    }

    protected void doEjbHome_FIND(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_HOME,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        /* Multiple instances found */
        if (result instanceof Collection) {

            final Object[] primaryKeys = ((Collection) result).toArray();

            for (int i = 0; i < primaryKeys.length; i++) {
                final ProxyInfo proxyInfo = ((ProxyInfo) primaryKeys[i]);
                if (proxyInfo == null) {
                    primaryKeys[i] = null;
                } else {
                    primaryKeys[i] = proxyInfo.getPrimaryKey();
                }
            }

            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK_FOUND_COLLECTION, primaryKeys);

        } else if (result instanceof java.util.Enumeration) {

            final java.util.Enumeration resultAsEnum = (java.util.Enumeration) result;
            final java.util.List<Object> listOfPKs = new ArrayList<Object>();
            while (resultAsEnum.hasMoreElements()) {
                final ProxyInfo proxyInfo = ((ProxyInfo) resultAsEnum.nextElement());
                if (proxyInfo == null) {
                    listOfPKs.add(null);
                } else {
                    listOfPKs.add(proxyInfo.getPrimaryKey());
                }
            }

            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK_FOUND_ENUMERATION, listOfPKs.toArray(new Object[listOfPKs.size()]));
            /* Single instance found */
        } else if (result instanceof ProxyInfo) {
            final ProxyInfo proxyInfo = ((ProxyInfo) result);
            result = proxyInfo.getPrimaryKey();
            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK_FOUND, result);
        } else if (result == null) {
            res.setResponse(req.getVersion(), ResponseCodes.EJB_OK_FOUND, null);
        } else {

            final String message = "The bean is not EJB compliant. " +
                "The finder method [" + req.getMethodInstance().getName() + "] is declared " +
                "to return neither Collection nor the Remote Interface, " +
                "but [" + result.getClass().getName() + "]";
            result = new RemoteException(message);
            logger.error(req + " " + message);
            res.setResponse(req.getVersion(), ResponseCodes.EJB_SYS_EXCEPTION, result);
        }
    }

    protected void doEjbObject_GET_EJB_HOME(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_GET_HANDLE(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_GET_PRIMARY_KEY(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_IS_IDENTICAL(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_REMOVE(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_OBJECT,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, null);
    }

    protected void doEjbHome_GET_EJB_META_DATA(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_GET_HOME_HANDLE(final EJBRequest req, final EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_REMOVE_BY_HANDLE(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_HOME,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, null);
    }

    protected void doEjbHome_REMOVE_BY_PKEY(final EJBRequest req, final EJBResponse res) throws Exception {

        final CallContext call = CallContext.getCallContext();
        final RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        c.invoke(
            req.getDeploymentId(),
            InterfaceType.EJB_HOME,
            req.getInterfaceClass(),
            req.getMethodInstance(),
            req.getMethodParameters(),
            req.getPrimaryKey()
        );

        res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, null);
    }

    protected void checkMethodAuthorization(final EJBRequest req, final EJBResponse res) throws Exception {
        res.setResponse(req.getVersion(), ResponseCodes.EJB_OK, null);
    }

    private EJBResponse setResponseError(final EJBResponse res, final byte version, final Throwable error, final String message) {

        //This is fatal for the client, but not the server.
        if (logger.isInfoEnabled()) {
            logger.info(message + " - Enable DEBUG for stacktrace: " + error);
        } else if (logger.isDebugEnabled()) {
            logger.debug(message, error);
        }

        final RemoteException re = new RemoteException(message, error);
        res.setResponse(version, ResponseCodes.EJB_ERROR, new ThrowableArtifact(re));
        return res;
    }
}
