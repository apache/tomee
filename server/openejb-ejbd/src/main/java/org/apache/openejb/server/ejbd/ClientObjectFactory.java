package org.apache.openejb.server.ejbd;

import java.net.URI;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.EJBHomeHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.ServerMetaData;

class ClientObjectFactory implements org.apache.openejb.spi.ApplicationServer {
    private final EjbDaemon daemon;

    protected ServerMetaData sMetaData;

    public ClientObjectFactory(EjbDaemon daemon) {

        try {
            this.sMetaData = new ServerMetaData(new URI("foo://"+"127.0.0.1" +":"+4201));
        } catch (Exception e) {

            e.printStackTrace();
        }
        this.daemon = daemon;
    }

    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBMetaData(call, info);
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHandle(call, info);
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHomeHandle(call, info);
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBObject(call, info);
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBHome(call, info);
    }

    protected javax.ejb.EJBMetaData _getEJBMetaData(CallContext call, ProxyInfo info) {

        DeploymentInfo deployment = info.getDeploymentInfo();
        int idCode = this.daemon.deploymentIndex.getDeploymentIndex(deployment);

        EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType(),
                deployment.getDeploymentID().toString(),
                idCode);
        return metaData;
    }

    protected javax.ejb.Handle _getHandle(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = this.daemon.deploymentIndex.getDeploymentIndex(deployment);

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {

        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType(),
                deployment.getDeploymentID().toString(),
                idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, sMetaData, cMetaData, primKey);

        return new EJBObjectHandle(hanlder.createEJBObjectProxy());
    }

    protected javax.ejb.HomeHandle _getHomeHandle(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = this.daemon.deploymentIndex.getDeploymentIndex(deployment);

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType(),
                deployment.getDeploymentID().toString(),
                idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, sMetaData, cMetaData);

        return new EJBHomeHandle(hanlder.createEJBHomeProxy());
    }

    protected javax.ejb.EJBObject _getEJBObject(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = this.daemon.deploymentIndex.getDeploymentIndex(deployment);

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType(),
                deployment.getDeploymentID().toString(),
                idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, sMetaData, cMetaData, primKey);

        return hanlder.createEJBObjectProxy();
    }

    protected javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = this.daemon.deploymentIndex.getDeploymentIndex(deployment);

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType(),
                deployment.getDeploymentID().toString(),
                idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, sMetaData, cMetaData);

        return hanlder.createEJBHomeProxy();
    }
}