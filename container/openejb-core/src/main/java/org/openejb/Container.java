package org.openejb;

import java.util.HashMap;
import java.util.Properties;

public interface Container {

    final public static int STATELESS = 1;
    final public static int STATEFUL = 2;
    final public static int ENTITY = 3;
    final public static int MESSAGE_DRIVEN = 4;

    public void init(Object containerId, HashMap deployments, Properties properties)
    throws OpenEJBException;

    public int getContainerType( );

    public Object getContainerID();

    public DeploymentInfo getDeploymentInfo(Object deploymentID);

    public DeploymentInfo [] deployments();

    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException;
}
