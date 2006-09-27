package org.apache.openejb.core;

import java.util.HashMap;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.core.ivm.naming.Reference;

/**
 * @org.apache.xbean.XBean element="containerSystem"
 */
public class ContainerSystem implements org.apache.openejb.spi.ContainerSystem {

    HashMap deployments = new HashMap();
    HashMap containers = new HashMap();
    IvmContext jndiRootContext = null;

    public ContainerSystem() {

        try {

            jndiRootContext = IvmContext.createRootContext();

            jndiRootContext.createSubcontext("java:openejb/ejb");
        }
        catch (javax.naming.NamingException exception) {
            throw new RuntimeException();
        }

        // todo this should be in a start method because publishing an external reference in the constructor is very dangerous
        SystemInstance.get().setComponent(org.apache.openejb.spi.ContainerSystem.class, this);
    }

    public DeploymentInfo getDeploymentInfo(Object id) {
        return (DeploymentInfo) deployments.get(id);
    }

    public DeploymentInfo [] deployments() {
        return (DeploymentInfo []) deployments.values().toArray(new DeploymentInfo [deployments.size()]);
    }

    public Container getContainer(Object id) {
        return (Container) containers.get(id);
    }

    public Container [] containers() {
        return (Container []) containers.values().toArray(new Container [containers.size()]);
    }

    public void addContainer(Object id, Container c) {
        containers.put(id, c);
    }

    public void addDeployment(org.apache.openejb.core.CoreDeploymentInfo deployment) {

        this.deployments.put(deployment.getDeploymentID(), deployment);

        if (deployment.getHomeInterface() != null) {
            bindProxy(deployment, deployment.getEJBHome(), false);
        }
        if (deployment.getLocalHomeInterface() != null) {
            bindProxy(deployment, deployment.getEJBLocalHome(), true);
        }
    }

    private void bindProxy(org.apache.openejb.core.CoreDeploymentInfo deployment, Object proxy, boolean isLocal) {
        Reference ref = new ObjectReference(proxy);

        if (deployment.getComponentType() == DeploymentInfo.STATEFUL) {
            ref = new org.apache.openejb.core.stateful.EncReference(ref);
        } else if (deployment.getComponentType() == DeploymentInfo.STATELESS) {
            ref = new org.apache.openejb.core.stateless.EncReference(ref);
        } else {
            ref = new org.apache.openejb.core.entity.EncReference(ref);
        }

        try {

            String bindName = deployment.getDeploymentID().toString();

            if (bindName.charAt(0) == '/') {
                bindName = bindName.substring(1);
            }

            bindName = "openejb/ejb/" + bindName;
            if (isLocal) {
                bindName += "Local";
            }
            jndiRootContext.bind(bindName, ref);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public javax.naming.Context getJNDIContext() {
        return jndiRootContext;
    }
}
