package org.openejb.spi;

import org.openejb.Container;
import org.openejb.DeploymentInfo;

public interface ContainerSystem {

    public DeploymentInfo getDeploymentInfo(Object id);

    public DeploymentInfo [] deployments( );

    public Container getContainer(Object id);

    public Container [] containers( );

    public javax.naming.Context getJNDIContext();
}