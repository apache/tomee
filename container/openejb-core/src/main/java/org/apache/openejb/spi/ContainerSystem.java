package org.apache.openejb.spi;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;

public interface ContainerSystem {

    public DeploymentInfo getDeploymentInfo(Object id);

    public DeploymentInfo [] deployments();

    public Container getContainer(Object id);

    public Container [] containers();

    public javax.naming.Context getJNDIContext();
}