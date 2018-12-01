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

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.config.sys.Container;

import java.util.ArrayList;
import java.util.List;

public class ContainerUtils {
    public static List<ContainerInfo> getContainerInfos(final AppModule module, final ConfigurationFactory configFactory) throws OpenEJBException {
        final List<ContainerInfo> containerInfos = new ArrayList<>();
        if (module.getContainers().isEmpty()) {
            return containerInfos;
        }

        final String prefix = module.getModuleId() + "/";
        for (final Container container : module.getContainers()) {
            if (container.getId() == null) {
                throw new IllegalStateException("a container can't get a null id: " + container.getType() + " from " + module.getModuleId());
            }
            if (!container.getId().startsWith(prefix)) {
                container.setId(prefix + container.getId());
            }

            final ContainerInfo containerInfo = configFactory.createContainerInfo(container);
            containerInfo.originAppName = module.getModuleId();

            final Object applicationWideProperty = containerInfo.properties.remove("ApplicationWide");
            if (applicationWideProperty != null) {
                containerInfo.applicationWide = Boolean.parseBoolean(applicationWideProperty.toString().trim());
            }

            containerInfos.add(containerInfo);
        }

        return containerInfos;
    }
}
