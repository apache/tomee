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
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.client.*;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class EjbRequestHandler {
    public static final ServerSideResolver SERVER_SIDE_RESOLVER = new ServerSideResolver();

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("ejb"), "org.apache.openejb.server.util.resources");
    private final EjbDaemon daemon;

    private final ClusterableRequestHandler clusterableRequestHandler;

    private Map<String, AtomicBoolean> asynchronousInvocationCancelMap = new ConcurrentHashMap<String, AtomicBoolean>();

    EjbRequestHandler(EjbDaemon daemon) {
        this.daemon = daemon;

        clusterableRequestHandler = newClusterableRequestHandler();
    }

    protected BasicClusterableRequestHandler newClusterableRequestHandler() {
        return new BasicClusterableRequestHandler();
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        // Setup the client proxy replacement to replace
        // the proxies with the IntraVM proxy implementations
        EJBHomeProxyHandle.resolver.set(SERVER_SIDE_RESOLVER);
        EJBObjectProxyHandle.resolver.set(SERVER_SIDE_RESOLVER);

        EJBRequest req = new EJBRequest();
        EJBResponse res = new EJBResponse();

        try {
            req.readExternal(in);
        } catch (Throwable t) {
            replyWithFatalError(out, t, "Bad request");
            return;
        }

        SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        try {
            Object clientIdentity = req.getClientIdentity();
            if (clientIdentity != null) securityService.associate(clientIdentity);
        } catch (Throwable t) {
            replyWithFatalError(out, t, "Client identity is not valid");
            return;
        }

        CallContext call = null;
        BeanContext di = null;

        try {
            di = this.daemon.getDeployment(req);
        } catch (RemoteException e) {
            replyWithFatalError(out, e, "No such deployment");
            return;
            /*
                logger.warn( req + "No such deployment: "+e.getMessage());
                res.setResponse( EJB_SYS_EXCEPTION, e);
                res.writeExternal( out );
                return;
            */
        } catch (Throwable t) {
            replyWithFatalError(out, t, "Unkown error occured while retrieving deployment");
            return;
        }

        //  Need to set this for deserialization of the body
        ClassLoader classLoader = di.getBeanClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        try {
            req.getBody().readExternal(in);
        } catch (Throwable t) {
            replyWithFatalError(out, t, "Error caught during request processing");
            return;
        }

        try {
            call = CallContext.getCallContext();
            call.setEJBRequest(req);
            call.setBeanContext(di);
        } catch (Throwable t) {
            replyWithFatalError(out, t, "Unable to set the thread context for this request");
            return;
        }

        boolean respond = true;
        try {
            switch (req.getRequestMethod()) {
                // Remote interface methods
                case RequestMethodConstants.EJB_OBJECT_BUSINESS_METHOD:
                    doEjbObject_BUSINESS_METHOD(req, res);
                    updateServer(req, res);
                    break;

                // Home interface methods
                case RequestMethodConstants.EJB_HOME_CREATE:
                    doEjbHome_CREATE(req, res);
                    updateServer(req, res);
                    break;

                // Home interface methods
                case RequestMethodConstants.EJB_HOME_METHOD:
                    doEjbHome_METHOD(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_HOME_FIND:
                    doEjbHome_FIND(req, res);
                    updateServer(req, res);
                    break;

                // javax.ejb.EJBObject methods
                case RequestMethodConstants.EJB_OBJECT_GET_EJB_HOME:
                    doEjbObject_GET_EJB_HOME(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_OBJECT_GET_HANDLE:
                    doEjbObject_GET_HANDLE(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_OBJECT_GET_PRIMARY_KEY:
                    doEjbObject_GET_PRIMARY_KEY(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_OBJECT_IS_IDENTICAL:
                    doEjbObject_IS_IDENTICAL(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_OBJECT_REMOVE:
                    doEjbObject_REMOVE(req, res);
                    break;

                // javax.ejb.EJBHome methods
                case RequestMethodConstants.EJB_HOME_GET_EJB_META_DATA:
                    doEjbHome_GET_EJB_META_DATA(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_HOME_GET_HOME_HANDLE:
                    doEjbHome_GET_HOME_HANDLE(req, res);
                    updateServer(req, res);
                    break;

                case RequestMethodConstants.EJB_HOME_REMOVE_BY_HANDLE:
                    doEjbHome_REMOVE_BY_HANDLE(req, res);
                    break;

                case RequestMethodConstants.EJB_HOME_REMOVE_BY_PKEY:
                    doEjbHome_REMOVE_BY_PKEY(req, res);
                    break;

                case RequestMethodConstants.FUTURE_CANCEL:
                    doFUTURE_CANCEL_METHOD(req, res);
                    break;
            }

        } catch (org.apache.openejb.InvalidateReferenceException e) {
            res.setResponse(ResponseCodes.EJB_SYS_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (org.apache.openejb.ApplicationException e) {
            res.setResponse(ResponseCodes.EJB_APP_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (org.apache.openejb.SystemException e) {
            res.setResponse(ResponseCodes.EJB_ERROR, new ThrowableArtifact(e.getRootCause()));
            logger.error(req + ": OpenEJB encountered an unknown system error in container: ", e);
        } catch (Throwable t) {

            replyWithFatalError(out, t, "Unknown error in container");
            respond = false;

        } finally {

            if (logger.isDebugEnabled()) {
                //The req and res toString overrides are volatile
                try {
                    logger.debug("EJB REQUEST: " + req + " -- RESPONSE: " + res);
                } catch (Throwable t) {
                    //Ignore
                }
            }

            if (respond) {
                try {
                    res.writeExternal(out);
                } catch (Throwable t) {
                    logger.error("Failed to write EjbResponse", t);
                }
            }

            try {
                securityService.disassociate();
            } catch (Throwable t) {
                logger.warning("Failed to disassociate security", t);
            }

            call.reset();
            EJBHomeProxyHandle.resolver.set(null);
            EJBObjectProxyHandle.resolver.set(null);
        }
    }

    protected void updateServer(EJBRequest req, EJBResponse res) {
        CallContext callContext = CallContext.getCallContext();
        BeanContext beanContext = callContext.getBeanContext();
        clusterableRequestHandler.updateServer(beanContext, req, res);
    }

    protected void doFUTURE_CANCEL_METHOD(EJBRequest req, EJBResponse res) throws Exception {
        AtomicBoolean invocationCancelTag = asynchronousInvocationCancelMap.get(req.getBody().getRequestId());
        if (invocationCancelTag == null) {
            //TODO ?
        } else {
            invocationCancelTag.set((Boolean) req.getBody().getMethodParameters()[0]);
            res.setResponse(ResponseCodes.EJB_OK, null);
        }
    }

    protected void doEjbObject_BUSINESS_METHOD(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = (BeanContext) call.getBeanContext();
        boolean asynchronous = beanContext.isAsynchronous(req.getMethodInstance());
        try {
            if (asynchronous) {
                AtomicBoolean invocationCancelTag = new AtomicBoolean(false);
                ThreadContext.initAsynchronousCancelled(invocationCancelTag);
                asynchronousInvocationCancelMap.put(req.getBody().getRequestId(), invocationCancelTag);
            }
            RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

            Object result = c.invoke(req.getDeploymentId(),
                    req.getInterfaceClass(), req.getMethodInstance(),
                    req.getMethodParameters(),
                    req.getPrimaryKey()
            );

            //Pass the internal value to the remote client, as AsyncResult is not serializable
            if (result != null && asynchronous) {
                result = ((Future) result).get();
            }

            res.setResponse(ResponseCodes.EJB_OK, result);
        } finally {
            if (asynchronous) {
                ThreadContext.removeAsynchronousCancelled();
                asynchronousInvocationCancelMap.remove(req.getBody().getRequestId());
            }
        }
    }

    protected void doEjbHome_METHOD(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        res.setResponse(ResponseCodes.EJB_OK, result);
    }

    protected void doEjbHome_CREATE(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        if (result instanceof ProxyInfo) {
            ProxyInfo info = (ProxyInfo) result;
            res.setResponse(ResponseCodes.EJB_OK, info.getPrimaryKey());
        } else {

            result = new RemoteException("The bean is not EJB compliant.  The bean should be created or and exception should be thrown.");
            logger.error(req + "The bean is not EJB compliant.  The bean should be created or and exception should be thrown.");
            res.setResponse(ResponseCodes.EJB_SYS_EXCEPTION, new ThrowableArtifact((Throwable) result));
        }
    }

    protected void doEjbHome_FIND(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        /* Multiple instances found */
        if (result instanceof Collection) {

            Object[] primaryKeys = ((Collection) result).toArray();

            for (int i = 0; i < primaryKeys.length; i++) {
                ProxyInfo proxyInfo = ((ProxyInfo) primaryKeys[i]);
                if (proxyInfo == null) {
                    primaryKeys[i] = null;
                } else {
                    primaryKeys[i] = proxyInfo.getPrimaryKey();
                }
            }

            res.setResponse(ResponseCodes.EJB_OK_FOUND_COLLECTION, primaryKeys);

        } else if (result instanceof java.util.Enumeration) {

            java.util.Enumeration resultAsEnum = (java.util.Enumeration) result;
            java.util.List<Object> listOfPKs = new ArrayList<Object>();
            while (resultAsEnum.hasMoreElements()) {
                ProxyInfo proxyInfo = ((ProxyInfo) resultAsEnum.nextElement());
                if (proxyInfo == null) {
                    listOfPKs.add(null);
                } else {
                    listOfPKs.add(proxyInfo.getPrimaryKey());
                }
            }

            res.setResponse(ResponseCodes.EJB_OK_FOUND_ENUMERATION, listOfPKs.toArray(new Object[listOfPKs.size()]));
            /* Single instance found */
        } else if (result instanceof ProxyInfo) {
            ProxyInfo proxyInfo = ((ProxyInfo) result);
            result = proxyInfo.getPrimaryKey();
            res.setResponse(ResponseCodes.EJB_OK_FOUND, result);
        } else if (result == null) {
            res.setResponse(ResponseCodes.EJB_OK_FOUND, null);
        } else {

            final String message = "The bean is not EJB compliant. " +
                    "The finder method [" + req.getMethodInstance().getName() + "] is declared " +
                    "to return neither Collection nor the Remote Interface, " +
                    "but [" + result.getClass().getName() + "]";
            result = new RemoteException(message);
            logger.error(req + " " + message);
            res.setResponse(ResponseCodes.EJB_SYS_EXCEPTION, result);
        }
    }

    protected void doEjbObject_GET_EJB_HOME(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_GET_HANDLE(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_GET_PRIMARY_KEY(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_IS_IDENTICAL(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbObject_REMOVE(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        res.setResponse(ResponseCodes.EJB_OK, null);
    }

    protected void doEjbHome_GET_EJB_META_DATA(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_GET_HOME_HANDLE(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_REMOVE_BY_HANDLE(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        res.setResponse(ResponseCodes.EJB_OK, null);
    }

    protected void doEjbHome_REMOVE_BY_PKEY(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getBeanContext().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getInterfaceClass(), req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey()
        );

        res.setResponse(ResponseCodes.EJB_OK, null);
    }

    protected void checkMethodAuthorization(EJBRequest req, EJBResponse res) throws Exception {
        res.setResponse(ResponseCodes.EJB_OK, null);
    }

    private void replyWithFatalError(ObjectOutputStream out, Throwable error, String message) {

        //This is fatal for the client, but not the server.
        if (logger.isWarningEnabled()) {
            logger.warning(message + " - Debug for stacktrace");
        } else if (logger.isDebugEnabled()) {
            logger.debug(message, error);
        }

        final RemoteException re = new RemoteException(message, error);
        final EJBResponse res = new EJBResponse();
        res.setResponse(ResponseCodes.EJB_ERROR, new ThrowableArtifact(re));

        try {
            res.writeExternal(out);
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to write EjbResponse", t);
            } else if (logger.isWarningEnabled()) {
                logger.warning("Failed to write EjbResponse - Debug for stacktrace");
            }
        }
    }
}
