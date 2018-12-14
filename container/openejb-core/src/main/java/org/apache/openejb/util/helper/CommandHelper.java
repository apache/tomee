/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util.helper;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.table.Line;
import org.apache.openejb.table.Lines;
import org.apache.openejb.util.JavaSecurityManagers;

import java.util.Collections;

public final class CommandHelper {
    private CommandHelper() {
        // no-op
    }

    public static Lines listEJBs() throws Exception {
        return listEJBs(JavaSecurityManagers.getSystemProperty("line.separator"));
    }

    public static Lines listEJBs(final String cr) throws Exception {
        final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        final Lines lines = new Lines(cr);
        lines.add(new Line("Name", "Class", "Interface Type", "Bean Type"));
        for (final BeanContext bc : cs.deployments()) {
            if (bc.isHidden()) {
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
            sb.append("Local").append(Collections.singletonList(bc.getBusinessLocalInterfaces()));
            empty = false;
        }
        if (bc.getBusinessRemoteInterface() != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("Remote").append(Collections.singletonList(bc.getBusinessRemoteInterfaces()));
        }
        return sb.toString();
    }
}
