package org.apache.openejb.monitoring;

import java.util.Map;
import javax.management.Description;
import javax.management.ManagedAttribute;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.assembler.classic.ContainerInfo;

@Description("describe a container")
public class JMXContainer {
    private final Container container;
    private final ContainerInfo info;

    public JMXContainer(final ContainerInfo serviceInfo, final Container service) {
        info = serviceInfo;
        container = service;
    }

    @ManagedAttribute
    @Description("Container id.")
    public String getContainerId() {
        return container.getContainerID().toString();
    }

    @ManagedAttribute
    @Description("Container type.")
    public String getContainerType() {
        return container.getContainerType().name().toLowerCase().replace("_", " ");
    }

    @ManagedAttribute
    @Description("Container managed beans.")
    public String[] getManagedBeans() {
        final BeanContext[] beans = container.getBeanContexts();
        final String[] beanNames = new String[beans.length];
        int i = 0;
        for (BeanContext bc : beans) {
            beanNames[i++] = new StringBuilder("bean-class: ").append(bc.getBeanClass().getName()).append(", ")
                    .append("ejb-name: ").append(bc.getEjbName()).append(", ")
                    .append("deployment-id: ").append(bc.getDeploymentID()).append(", ")
                    .toString();
        }
        return beanNames;
    }

    @ManagedAttribute
    @Description("Container service.")
    public String getService() {
        return info.service;
    }

    @ManagedAttribute
    @Description("Container class name.")
    public String getClassName() {
        return info.className;
    }

    @ManagedAttribute
    @Description("Container factory method.")
    public String getFactoryMethod() {
        return info.factoryMethod;
    }

    @ManagedAttribute
    @Description("Container properties.")
    public String[] getProperties() {
        final String[] properties = new String[info.properties.size()];
        int i = 0;
        for (Map.Entry<Object, Object> entry : info.properties.entrySet()) {
            properties[i++] = new StringBuilder(entry.getKey().toString())
                    .append(" = ").append(entry.getValue().toString())
                    .toString();
        }
        return properties;
    }
}
