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

import org.apache.openejb.Container;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.core.mdb.MdbContainer;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ActivationConfigPropertyOverride implements DynamicDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ActivationConfigPropertyOverride.class);

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        final Properties system = new Properties();
        system.putAll(SystemInstance.get().getProperties());
        system.putAll(appModule.getProperties());
        system.putAll(JavaSecurityManagers.getSystemProperties());

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final EjbJar ejbJar = ejbModule.getEjbJar();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            final Properties module = new Properties();
            module.putAll(openejbJar.getProperties());
            module.putAll(system);

            final Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

            for (final EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {

                final String ejbName = bean.getEjbName();
                final EjbDeployment ejbDeployment = deployments.get(ejbName);

                if (!(bean instanceof MessageDrivenBean)) {
                    continue;
                }


                final Properties properties = new Properties();
                properties.putAll(module);
                properties.putAll(ejbDeployment.getProperties());

                final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                // override with placeholding
                if (mdb.getActivationConfig() != null) {
                    for (final ActivationConfigProperty property : mdb.getActivationConfig().getActivationConfigProperty()) {
                        final String originalValue = property.getActivationConfigPropertyValue();
                        final String value = PropertyPlaceHolderHelper.simpleValue(originalValue);
                        if (value != null && !originalValue.equals(value)) {
                            property.setActivationConfigPropertyValue(value);
                        }
                    }
                }
                // now try to use special keys
                final Properties overrides = new Properties();

                final MdbContainerDetails mdbContainer = getMdbContainer(appModule, ejbDeployment.getContainerId(), appModule.getModuleId());
                if (mdbContainer != null) {
                    overrides.putAll(ConfigurationFactory.getOverrides(properties, "mdb.container." + mdbContainer.getContainerId() + ".activation", "EnterpriseBean"));
                    overrides.putAll(ConfigurationFactory.getOverrides(mdbContainer.getProperties(), "activation", "EnterpriseBean"));
                }

                overrides.putAll(ConfigurationFactory.getOverrides(properties, "mdb.activation", "EnterpriseBean"));
                overrides.putAll(ConfigurationFactory.getOverrides(properties, mdb.getMessagingType() + ".activation", "EnterpriseBean"));
                overrides.putAll(ConfigurationFactory.getOverrides(properties, ejbName + ".activation", "EnterpriseBean"));
                overrides.putAll(ConfigurationFactory.getOverrides(properties, ejbDeployment.getDeploymentId() + ".activation", "EnterpriseBean"));


                // If we don't have any overrides, skip to the next
                if (overrides.size() == 0) {
                    continue;
                }

                if (mdb.getActivationConfig() == null) {
                    mdb.setActivationConfig(new ActivationConfig());
                }
                final List<ActivationConfigProperty> activationConfigList = mdb.getActivationConfig().getActivationConfigProperty();

                for (final Map.Entry<Object, Object> entry : overrides.entrySet()) {

                    final Object property = String.valueOf(entry.getKey());
                    final Object value = String.valueOf(entry.getValue());

                    ActivationConfigProperty activationConfigProperty = this.findActivationProperty(activationConfigList, property.toString());

                    if (activationConfigProperty != null) {
                        logger.info(String.format("Found %s bean with activation-config property %s=%s to override", ejbName, activationConfigProperty.getActivationConfigPropertyName(), activationConfigProperty.getActivationConfigPropertyValue()));
                        logger.info(String.format("Overriding %s bean activation-config property.%s=%s", ejbName, property, value));
                        activationConfigProperty.setActivationConfigPropertyValue(entry.getValue().toString());

                    } else {
                        logger.info(String.format("Adding %s bean activation-config property %s=%s", ejbName, property, value));
                        activationConfigProperty = new ActivationConfigProperty();
                        activationConfigProperty.setActivationConfigPropertyName(property.toString());
                        activationConfigProperty.setActivationConfigPropertyValue(value.toString());
                        activationConfigList.add(activationConfigProperty);
                    }

                }
            }
        }

        return appModule;
    }

    private MdbContainerDetails getMdbContainer(final AppModule appModule, final String containerId, final String moduleId) {

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final ConfigurationFactory configurationFactory = SystemInstance.get().getComponent(ConfigurationFactory.class);

        if (containerId == null || containerId.length() == 0) {
            final Container[] containers = containerSystem.containers();
            for (Container container : containers) {
                if (MdbContainer.class.isInstance(container)) {
                    return convert(MdbContainer.class.cast(container));
                }
            }

            // not found a container, try config factory
            final List<ContainerInfo> containerInfos = configurationFactory.getContainerInfos();
            for (final ContainerInfo containerInfo : containerInfos) {
                if (MdbContainerInfo.class.isInstance(containerInfo)) {
                    return convert(MdbContainerInfo.class.cast(containerInfo));
                }
            }

            return null;
        }

        final Container appContainer = containerSystem.getContainer(moduleId + "/" + containerId);
        if (appContainer != null && MdbContainer.class.isInstance(appContainer)) {
            return convert(MdbContainer.class.cast(appContainer));
        }

        final MdbContainerDetails appContainerInfo = findContainerInfo(configurationFactory.getContainerInfos(), moduleId + "/" + containerId);
        if (appContainerInfo != null) {
            return appContainerInfo;
        }

        final Container container = containerSystem.getContainer(containerId);
        if (MdbContainer.class.isInstance(container)) {
            return convert(MdbContainer.class.cast(container));
        }

        final MdbContainerDetails containerInfo = findContainerInfo(configurationFactory.getContainerInfos(), containerId);
        if (containerInfo != null) {
            return containerInfo;
        }

        final MdbContainerDetails moduleContainer = findModuleContainer(appModule, configurationFactory, containerId);
        if (moduleContainer != null) {
            return moduleContainer;
        }

        return null;
    }

    private MdbContainerDetails findModuleContainer(final AppModule appModule, final ConfigurationFactory configurationFactory, final String containerId) {
        // try the containers on the AppModule
        final Collection<org.apache.openejb.config.sys.Container> containers = appModule.getContainers();
        for (final org.apache.openejb.config.sys.Container appMopduleContainer : containers) {
            if (appMopduleContainer.getId().equals(containerId) || appMopduleContainer.getId().equals(appModule.getModuleId() + "/" + containerId)) {
                try {
                    final ContainerInfo containerInfo = configurationFactory.createContainerInfo(appMopduleContainer);
                    if (containerInfo != null && MdbContainerInfo.class.isInstance(containerInfo)) {
                        return convert(MdbContainerInfo.class.cast(containerInfo));
                    }
                } catch (OpenEJBException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private MdbContainerDetails convert(final MdbContainerInfo mdbContainerInfo) {
        return new MdbContainerDetails(mdbContainerInfo.id, mdbContainerInfo.properties);
    }

    private MdbContainerDetails findContainerInfo(final List<ContainerInfo> containerInfos, final String id) {
        for (final ContainerInfo containerInfo : containerInfos) {
            if (MdbContainerInfo.class.isInstance(containerInfo) && containerInfo.id.equals(id)) {
                return new MdbContainerDetails(containerInfo.id, containerInfo.properties);
            }
        }

        return null;
    }

    private MdbContainerDetails convert(final MdbContainer mdbContainer) {
        if (mdbContainer == null) {
            return null;
        }

        if (mdbContainer.getContainerID() == null) {
            throw new IllegalStateException("Container has no ID");
        }

        return new MdbContainerDetails(mdbContainer.getContainerID().toString(), mdbContainer.getProperties());
    }


    private ActivationConfigProperty findActivationProperty(final List<ActivationConfigProperty> activationConfigList, final String nameOfProperty) {
        for (final ActivationConfigProperty activationProp : activationConfigList) {
            if (activationProp.getActivationConfigPropertyName().equals(nameOfProperty)) {
                return activationProp;
            }
        }

        return null;
    }

    private static class MdbContainerDetails {
        private final String containerId;
        private final Properties properties;

        public MdbContainerDetails(String containerId, Properties properties) {
            this.containerId = containerId;
            this.properties = properties;
        }

        public String getContainerId() {
            return containerId;
        }

        public Properties getProperties() {
            return properties;
        }
    }
}
