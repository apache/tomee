package org.apache.openejb;

public class ProxyInfo {

    protected DeploymentInfo deploymentInfo;
    protected Object primaryKey;
    protected Class type;
    protected RpcContainer beanContainer;

    protected ProxyInfo() {
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, Class intrfc, RpcContainer container) {
        deploymentInfo = depInfo;
        primaryKey = pk;
        type = intrfc;
        beanContainer = container;
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, boolean isLocalInterface, RpcContainer container) {
        this.deploymentInfo = depInfo;
        this.primaryKey = pk;
        this.beanContainer = container;
        if (isLocalInterface) {
            this.type = deploymentInfo.getLocalInterface();
        } else {
            this.type = deploymentInfo.getRemoteInterface();
        }
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Class getInterface() {
        return type;
    }

    public RpcContainer getBeanContainer() {
        return beanContainer;
    }
}
