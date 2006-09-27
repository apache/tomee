package org.apache.openejb.assembler.classic;

public class EntityContainerInfo extends ContainerInfo {

    public EntityBeanInfo[] beans;

    public EntityContainerInfo() {
        containerType = ENTITY_CONTAINER;
    }

}
