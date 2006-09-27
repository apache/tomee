package org.apache.openejb.assembler.classic;

public class ContainerSystemInfo extends InfoObject {

    public ContainerInfo[] containers;
    public EnterpriseBeanInfo[] enterpriseBeans;
    public EjbJarInfo[] ejbJars;

    public EntityContainerInfo[] entityContainers;
    public StatelessSessionContainerInfo[] statelessContainers;
    public StatefulSessionContainerInfo[] statefulContainers;
    public SecurityRoleInfo[] securityRoles;
    public MethodPermissionInfo[] methodPermissions;
    public MethodTransactionInfo[] methodTransactions;
}
