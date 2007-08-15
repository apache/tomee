/**
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
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.JndiConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;

/**
 * Read in all the properties for an app's META-INF/env-entries.properties file
 * and create <env-entry> tags for each in the component's enc.
 *
 * @version $Rev$ $Date$
 */
public class EnvEntriesPropertiesDeployer implements DynamicDeployer {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, EnvEntriesPropertiesDeployer.class);

    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        // ApplicationClient META-INF/env-entries.properties
        for (ClientModule module : appModule.getClientModules()) {
            for (Map.Entry<String, String> entry : getEnvEntries(module).entrySet()) {
                EnvEntry envEntry = new EnvEntry(entry.getKey(), "java.lang.String", entry.getValue());
                apply(module.getApplicationClient(), envEntry, "AppClient");
            }
        }

        // EjbJar META-INF/env-entries.properties
        for (EjbModule module : appModule.getEjbModules()) {
            for (Map.Entry<String, String> entry : getEnvEntries(module).entrySet()) {
                EnvEntry envEntry = new EnvEntry(entry.getKey(), "java.lang.String", entry.getValue());

                // To override a specific ejb only
                // the following format is used:
                // <ejb-name>.name = value
                if (envEntry.getName().contains(".")) {
                    String name = envEntry.getName();
                    String ejbName = name.substring(0,name.indexOf('.'));
                    name = name.substring(name.indexOf('.')+1);
                    EnterpriseBean bean = module.getEjbJar().getEnterpriseBean(ejbName);
                    if (bean != null){
                        // Set the new property name without the <ejb-name>. prefix
                        envEntry.setName(name);
                        apply(bean, envEntry, bean.getEjbName());
                        continue;
                    }
                }

                for (EnterpriseBean bean : module.getEjbJar().getEnterpriseBeans()) {
                    apply(bean, envEntry, bean.getEjbName());
                }
            }
        }

        return appModule;
    }

    private void apply(JndiConsumer bean, EnvEntry newEntry, String componentName) {
        List<EnvEntry> entries = bean.getEnvEntry();

        // If this is an override to an existing entry, apply
        // it and return
        for (EnvEntry entry : entries) {
            if (entry.getName().equals(newEntry.getName())){
                log.debug("envprops.override", componentName, entry.getName(), entry.getEnvEntryValue(), newEntry.getEnvEntryValue());
                entry.setEnvEntryValue(newEntry.getEnvEntryValue());
                return;
            }
        }

        // Must not be an override, just add the new entry
        log.debug("envprops.add", componentName, newEntry.getName(), newEntry.getEnvEntryValue());
        entries.add(newEntry);
    }

    private Map<String, String> getEnvEntries(DeploymentModule module) {
        URL propsUrl = (URL) module.getAltDDs().get("env-entries.properties");
        if (propsUrl == null) return Collections.EMPTY_MAP;
        try {

            InputStream in = propsUrl.openStream();
            Properties envEntriesProps = new Properties();
            envEntriesProps.load(in);

            return new HashMap(envEntriesProps);
        } catch (IOException e) {
            log.error("envprops.notLoaded", e, propsUrl.toExternalForm());
            return Collections.EMPTY_MAP;
        }
    }
}
