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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SuperProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class BeanProperties implements DynamicDeployer {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ModuleProperties.class);

    private final Map<String, Properties> additionalProperties = new HashMap<String, Properties>();
    private final Properties globalProperties = new Properties();

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        final Properties base = new Properties();
        base.putAll(SystemInstance.get().getProperties());
        base.putAll(appModule.getProperties());

        for (EjbModule module : appModule.getEjbModules()) {
            final Properties overrides = new SuperProperties().caseInsensitive(true);
            overrides.putAll(base);
            overrides.putAll(module.getProperties());

            if (module.getOpenejbJar() == null) {
                module.setOpenejbJar(new OpenejbJar());
            }

            final OpenejbJar openejbJar = module.getOpenejbJar();

            final Map<String, EjbDeployment> deploymentMap = openejbJar.getDeploymentsByEjbName();
            for (EnterpriseBean bean : module.getEjbJar().getEnterpriseBeans()) {
                final SuperProperties properties = new SuperProperties().caseInsensitive(true);

                properties.putAll(globalProperties);

                final String additionalKey = bean.getEjbName();
                if (additionalProperties.containsKey(additionalKey)) {
                    for (Map.Entry<Object, Object> entry : additionalProperties.get(additionalKey).entrySet()) {
                        properties.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }

                final EjbDeployment deployment = deploymentMap.get(bean.getEjbName());
                if (deployment != null) {
                    properties.putAll(deployment.getProperties());
                    deployment.getProperties().clear();
                }

                final String id = bean.getEjbName() + ".";

                for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
                    final String key = entry.getKey().toString();

                    if (key.startsWith(id)) {
                        final String property = key.substring(id.length());

                        if (properties.containsKey(property)) {
                            log.debug("Overriding ejb " + bean.getEjbName() + " property " + property + "=" + entry.getValue());
                        } else {
                            log.debug("Adding ejb " + bean.getEjbName() + " property " + property + "=" + entry.getValue());
                        }

                        properties.put(property, entry.getValue());
                    }
                }

                if (properties.size() > 0) {
                    if (deployment == null) {
                        final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
                        ejbDeployment.getProperties().putAll(properties);
                    } else {
                        deployment.getProperties().putAll(properties);
                    }
                }
            }
        }

        // cleanup
        additionalProperties.clear();
        globalProperties.clear();

        return appModule;
    }

    public void addProperties(final String id, final Properties properties) {
        if (additionalProperties.containsKey(id)) {
            additionalProperties.get(id).putAll(properties);
        } else {
            additionalProperties.put(id, properties);
        }
    }

    public void addGlobalProperties(final Properties properties) {
        globalProperties.putAll(properties);
    }

    public void addGlobalProperties(final String prefix, final Properties properties) {
        if (prefix == null || prefix.isEmpty()) {
            addGlobalProperties(properties);
        } else {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                globalProperties.put(prefix + "." + entry.getKey(), entry.getValue());
            }
        }
    }
}
