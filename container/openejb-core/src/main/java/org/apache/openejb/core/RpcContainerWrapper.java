package org.apache.openejb.core;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.transaction.TransactionContainer;

import java.lang.reflect.Method;

public class RpcContainerWrapper implements RpcContainer, TransactionContainer {

    private final RpcContainer container;

    public RpcContainerWrapper(RpcContainer container) {
        this.container = container;
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return container.invoke(deployID, callMethod, args, primKey, securityIdentity);
    }

    public int getContainerType() {
        return container.getContainerType();
    }

    public Object getContainerID() {
        return container.getContainerID();
    }

    public org.apache.openejb.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return container.getDeploymentInfo(deploymentID);
    }

    public org.apache.openejb.DeploymentInfo [] deployments() {
        return container.deployments();
    }

    public void deploy(Object deploymentID, org.apache.openejb.DeploymentInfo info) throws OpenEJBException {
        container.deploy(deploymentID, info);
    }

    public void discardInstance(Object instance, ThreadContext context) {
        ((TransactionContainer) container).discardInstance(instance, context);
    }

    public RpcContainer getContainer() {
        return container;
    }

}
