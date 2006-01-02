package org.openejb.alt.assembler.classic;

public class StatelessSessionContainerInfo extends ContainerInfo {

    public StatelessBeanInfo[] beans;

    public StatelessSessionContainerInfo() {
        containerType = STATELESS_SESSION_CONTAINER;
    }

}
