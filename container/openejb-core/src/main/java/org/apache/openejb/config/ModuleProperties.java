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
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ModuleProperties implements DynamicDeployer {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ModuleProperties.class);

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        final Properties overrides = new Properties();
        overrides.putAll(SystemInstance.get().getProperties());
        overrides.putAll(appModule.getProperties());

        for (DeploymentModule module : appModule.getDeploymentModule()) {

            readProperties(module);

            applyOverrides(overrides, module);

        }

        return appModule;
    }

    private static void readProperties(DeploymentModule module) throws OpenEJBException {
        final Object o = module.getAltDDs().get("module.properties");

        if (o instanceof URL) {
            final URL url = (URL) o;
            try {
                final Properties properties = IO.readProperties(url);
                module.getProperties().putAll(properties);
            } catch (IOException e) {
                throw new OpenEJBException("Cannot read module.properties: " + url, e);
            }
        } else if (o instanceof Properties) {
            module.getProperties().putAll((Properties) o);
        } else if (o != null) {
            throw new OpenEJBException("Unknown module.properties type: "+o.getClass().getName());
        }
    }

    private static void applyOverrides(Properties overrides, DeploymentModule module) {
        final String id = module.getModuleId() + ".";

        for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
            final String key = entry.getKey().toString();

            if (key.startsWith(id)) {
                final String property = key.substring(id.length());

                if (module.getProperties().containsKey(property)) {
                    log.debug("Overriding module " + module.getModuleId() + " property " + property + "=" + entry.getValue());
                } else {
                    log.debug("Adding module " + module.getModuleId() + " property " + property + "=" + entry.getValue());
                }

                module.getProperties().put(property, entry.getValue());
            }
        }
    }
}
