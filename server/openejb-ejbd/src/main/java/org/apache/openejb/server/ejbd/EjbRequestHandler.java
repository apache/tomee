package org.apache.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.EJBResponse;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.ThrowableArtifact;
import org.apache.openejb.spi.SecurityService;

class EjbRequestHandler implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;

    EjbRequestHandler(EjbDaemon daemon) {
        this.daemon = daemon;

    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        EJBRequest req = new EJBRequest();
        EJBResponse res = new EJBResponse();

        try {
            req.readExternal(in);

            /*
                } catch (java.io.WriteAbortedException e){
                    if ( e.detail instanceof java.io.NotSerializableException){

                        throw new Exception("Client attempting to serialize unserializable object: "+ e.detail.getMessage());
                    } else {
                        throw e.detail;
                    }
                } catch (java.io.EOFException e) {
                    throw new Exception("Reached the end of the stream before the full request could be read");
                } catch (Throwable t){
                    throw new Exception("Cannot read client request: "+ t.getClass().getName()+" "+ t.getMessage());
                }
            */

        } catch (Throwable t) {
            replyWithFatalError
                    (out, t, "Error caught during request processing");
            return;
        }

        CallContext call = null;
        DeploymentInfo di = null;
        RpcContainer c = null;
        ;

        try {
            di = this.daemon.getDeployment(req);
        } catch (RemoteException e) {
            replyWithFatalError
                    (out, e, "No such deployment");
            return;
            /*
                logger.warn( req + "No such deployment: "+e.getMessage());
                res.setResponse( EJB_SYS_EXCEPTION, e);
                res.writeExternal( out );
                return;
            */
        } catch (Throwable t) {
            replyWithFatalError
                    (out, t, "Unkown error occured while retrieving deployment");
            return;
        }

        try {
            call = CallContext.getCallContext();
            call.setEJBRequest(req);
            call.setDeploymentInfo(di);
        } catch (Throwable t) {
            replyWithFatalError
                    (out, t, "Unable to set the thread context for this request");
            return;
        }

        try {
            switch (req.getRequestMethod()) {
            // Remote interface methods
                case EJB_OBJECT_BUSINESS_METHOD:
                    doEjbObject_BUSINESS_METHOD(req, res);
                    break;

                // Home interface methods
                case EJB_HOME_CREATE:
                    doEjbHome_CREATE(req, res);
                    break;

                case EJB_HOME_FIND:
                    doEjbHome_FIND(req, res);
                    break;

                // javax.ejb.EJBObject methods
                case EJB_OBJECT_GET_EJB_HOME:
                    doEjbObject_GET_EJB_HOME(req, res);
                    break;

                case EJB_OBJECT_GET_HANDLE:
                    doEjbObject_GET_HANDLE(req, res);
                    break;

                case EJB_OBJECT_GET_PRIMARY_KEY:
                    doEjbObject_GET_PRIMARY_KEY(req, res);
                    break;

                case EJB_OBJECT_IS_IDENTICAL:
                    doEjbObject_IS_IDENTICAL(req, res);
                    break;

                case EJB_OBJECT_REMOVE:
                    doEjbObject_REMOVE(req, res);
                    break;

                // javax.ejb.EJBHome methods
                case EJB_HOME_GET_EJB_META_DATA:
                    doEjbHome_GET_EJB_META_DATA(req, res);
                    break;

                case EJB_HOME_GET_HOME_HANDLE:
                    doEjbHome_GET_HOME_HANDLE(req, res);
                    break;

                case EJB_HOME_REMOVE_BY_HANDLE:
                    doEjbHome_REMOVE_BY_HANDLE(req, res);
                    break;

                case EJB_HOME_REMOVE_BY_PKEY:
                    doEjbHome_REMOVE_BY_PKEY(req, res);
                    break;
            }

        } catch (org.apache.openejb.InvalidateReferenceException e) {
            res.setResponse(EJB_SYS_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (org.apache.openejb.ApplicationException e) {
            res.setResponse(EJB_APP_EXCEPTION, new ThrowableArtifact(e.getRootCause()));
        } catch (org.apache.openejb.SystemException e) {
            res.setResponse(EJB_ERROR, new ThrowableArtifact(e.getRootCause()));

            this.daemon.logger.fatal(req + ": OpenEJB encountered an unknown system error in container: ", e);
        } catch (java.lang.Throwable t) {

            replyWithFatalError
                    (out, t, "Unknown error in container");
            return;
        } finally {
            this.daemon.logger.info("EJB RESPONSE: " + res);
            try {
                res.writeExternal(out);
            } catch (java.io.IOException ie) {
                this.daemon.logger.fatal("Couldn't write EjbResponse to output stream", ie);
            }
            call.reset();
        }
    }

    protected void doEjbObject_BUSINESS_METHOD(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        if (result instanceof ProxyInfo) {
            ProxyInfo info = (ProxyInfo) result;

            if (EJBObject.class.isAssignableFrom(info.getInterface())) {
                result = this.daemon.clientObjectFactory._getEJBObject(call, info);
            } else if (EJBHome.class.isAssignableFrom(info.getInterface())) {
                result = this.daemon.clientObjectFactory._getEJBHome(call, info);
            } else {

                result = new RemoteException("The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: " + info.getInterface());
                this.daemon.logger.error(req + "The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: " + info.getInterface());
                res.setResponse(EJB_SYS_EXCEPTION, result);
                return;
            }
        }

        res.setResponse(EJB_OK, result);
    }

    protected void doEjbHome_CREATE(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        if (result instanceof ProxyInfo) {
            ProxyInfo info = (ProxyInfo) result;
            res.setResponse(EJB_OK, info.getPrimaryKey());
        } else {

            result = new RemoteException("The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            this.daemon.logger.error(req + "The bean is not EJB compliant.  The should be created or and exception should be thrown.");
            res.setResponse(EJB_SYS_EXCEPTION, result);
        }
    }

    protected void doEjbHome_FIND(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        /* Multiple instances found */
        if (result instanceof Collection) {

            Object [] primaryKeys = ((Collection) result).toArray();

            for (int i = 0; i < primaryKeys.length; i++) {
                primaryKeys[i] = ((ProxyInfo) primaryKeys[i]).getPrimaryKey();
            }

            res.setResponse(EJB_OK_FOUND_COLLECTION, primaryKeys);

        } else if (result instanceof java.util.Enumeration) {

            java.util.Enumeration resultAsEnum = (java.util.Enumeration) result;
            java.util.List listOfPKs = new java.util.ArrayList();
            while (resultAsEnum.hasMoreElements()) {
                listOfPKs.add(((ProxyInfo) resultAsEnum.nextElement()).getPrimaryKey());
            }

            res.setResponse(EJB_OK_FOUND_ENUMERATION, listOfPKs.toArray(new Object[listOfPKs.size()]));
            /* Single instance found */
        } else if (result instanceof ProxyInfo) {
            result = ((ProxyInfo) result).getPrimaryKey();
            res.setResponse(EJB_OK_FOUND, result);

        } else {

            final String message = "The bean is not EJB compliant. " +
                    "The finder method [" + req.getMethodInstance().getName() + "] is declared " +
                    "to return neither Collection nor the Remote Interface, " +
                    "but [" + result.getClass().getName() + "]";
            result = new RemoteException(message);
            this.daemon.logger.error(req + " " + message);
            res.setResponse(EJB_SYS_EXCEPTION, result);
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
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        res.setResponse(EJB_OK, null);
    }

    protected void doEjbHome_GET_EJB_META_DATA(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_GET_HOME_HANDLE(EJBRequest req, EJBResponse res) throws Exception {
        checkMethodAuthorization(req, res);
    }

    protected void doEjbHome_REMOVE_BY_HANDLE(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        res.setResponse(EJB_OK, null);
    }

    protected void doEjbHome_REMOVE_BY_PKEY(EJBRequest req, EJBResponse res) throws Exception {

        CallContext call = CallContext.getCallContext();
        RpcContainer c = (RpcContainer) call.getDeploymentInfo().getContainer();

        Object result = c.invoke(req.getDeploymentId(),
                req.getMethodInstance(),
                req.getMethodParameters(),
                req.getPrimaryKey(),
                req.getClientIdentity());

        res.setResponse(EJB_OK, null);
    }

    protected void checkMethodAuthorization(EJBRequest req, EJBResponse res) throws Exception {

        SecurityService sec = (SecurityService) SystemInstance.get().getComponent(SecurityService.class);
        CallContext caller = CallContext.getCallContext();
        DeploymentInfo di = caller.getDeploymentInfo();
        String[] authRoles = di.getAuthorizedRoles(req.getMethodInstance());

        if (sec.isCallerAuthorized(req.getClientIdentity(), authRoles)) {
            res.setResponse(EJB_OK, null);
        } else {
            this.daemon.logger.info(req + "Unauthorized Access by Principal Denied");
            res.setResponse(EJB_APP_EXCEPTION, new RemoteException("Unauthorized Access by Principal Denied"));
        }
    }

    private void replyWithFatalError(ObjectOutputStream out, Throwable error, String message) {
        this.daemon.logger.fatal(message, error);
        RemoteException re = new RemoteException
                ("The server has encountered a fatal error: " + message + " " + error);
        EJBResponse res = new EJBResponse();
        res.setResponse(EJB_ERROR, re);
        try {
            res.writeExternal(out);
        } catch (java.io.IOException ie) {
            this.daemon.logger.error("Failed to write to EJBResponse", ie);
        }
    }
}
