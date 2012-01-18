package org.apache.openejb.util.helper;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.table.Line;
import org.apache.openejb.table.Lines;

import java.util.Arrays;

public class CommandHelper {
    private CommandHelper() {
        // no-op
    }

    public static Lines listEJBs() throws Exception {
        return listEJBs(System.getProperty("line.separator"));
    }

    public static Lines listEJBs(final String cr) throws Exception {
        final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        Lines lines = new Lines(cr);
        lines.add(new Line("Name", "Class", "Interface Type", "Bean Type"));
        for (BeanContext bc : cs.deployments()) {
            if (BeanContext.Comp.class.equals(bc.getBeanClass())) {
                continue;
            }

            lines.add(new Line(bc.getEjbName(), bc.getBeanClass().getName(), getType(bc), componentType(bc.getComponentType())));
        }

        return lines;
    }

    private static String componentType(final BeanType componentType) {
        if (componentType == null) {
            return "unknown";
        }
        return componentType.name();
    }

    private static String getType(final BeanContext bc) {
        boolean empty = true;
        final StringBuilder sb = new StringBuilder();
        if (bc.isLocalbean()) {
            sb.append("LocalBean[").append(bc.getBeanClass()).append("]");
            empty = false;
        }
        if (bc.getBusinessLocalInterface() != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("Local").append(Arrays.asList(bc.getBusinessLocalInterfaces()));
            empty = false;
        }
        if (bc.getBusinessRemoteInterface() != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("Remote").append(Arrays.asList(bc.getBusinessRemoteInterfaces()));
        }
        return sb.toString();
    }
}
