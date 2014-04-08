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
import org.apache.openejb.util.SuperProperties;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * TODO Check for app.properties
 * @version $Rev$ $Date$
 */
public class ApplicationProperties implements DynamicDeployer {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ApplicationProperties.class);

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        readPropertiesFiles(appModule);

        applyOverrides(appModule);

        return appModule;
    }

    private void readPropertiesFiles(AppModule appModule) throws OpenEJBException {
        final Collection<DeploymentModule> deploymentModule = appModule.getDeploymentModule();

        // We intentionally add the AppModule itself LAST so its properties trump all
        deploymentModule.add(appModule);

        for (DeploymentModule module : deploymentModule) {

            final Object o = module.getAltDDs().get("application.properties");

            if (o instanceof URL) {
                final URL url = (URL) o;
                try {
                    final Properties properties = IO.readProperties(url);
                    appModule.getProperties().putAll(properties);
                } catch (IOException e) {
                    throw new OpenEJBException("Cannot read application.properties: " + url, e);
                }
            } else if (o instanceof Properties) {
                appModule.getProperties().putAll((Properties) o);
            } else if (o != null) {
                throw new OpenEJBException("Unknown application.properties type: "+o.getClass().getName());
            }
        }


    }

    private void applyOverrides(AppModule appModule) {
        final String id = appModule.getModuleId() + ".";

        final Properties properties = SystemInstance.get().getProperties();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String key = entry.getKey().toString();

            if (key.startsWith(id)) {
                final String property = key.substring(id.length());

                if (appModule.getProperties().containsKey(property)) {
                    log.debug("Overriding application " + appModule.getModuleId() + " property " + property + "=" + entry.getValue());
                } else {
                    log.debug("Adding application " + appModule.getModuleId() + " property " + property + "=" + entry.getValue());
                }

                appModule.getProperties().put(property, entry.getValue());
            }
        }
    }

}
