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
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Read in all the properties for an app's META-INF/env-entries.properties file
 * and create <env-entry> tags for each in the component's enc.
 *
 * @version $Rev$ $Date$
 */
public class EnvEntriesPropertiesDeployer implements DynamicDeployer {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, EnvEntriesPropertiesDeployer.class);

    public static final String ENV_ENTRY_PROPERTIES = "env-entries.properties";

    private final String descriptorName;
    private final Map<String, String> additionalEnvEntries = new HashMap<>();

    public EnvEntriesPropertiesDeployer() {
        this(ENV_ENTRY_PROPERTIES);
    }

    public EnvEntriesPropertiesDeployer(final String descriptor) {
        descriptorName = descriptor;
    }

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        // ApplicationClient META-INF/env-entries.properties
        for (final ClientModule module : appModule.getClientModules()) {
            if (module.getApplicationClient() == null) {
                continue;
            }
            for (final Map.Entry<String, String> entry : getEnvEntries(module).entrySet()) {
                final EnvEntry envEntry = new EnvEntry(entry.getKey(), "java.lang.String", entry.getValue());
                apply(module.getApplicationClient(), envEntry, "AppClient");
            }
        }

        // WebModule META-INF/env-entries.properties
        for (final WebModule webModule : appModule.getWebModules()) {
            deploy(webModule);
        }

        // Resource Adapters do not have an ENC

        // EjbJar META-INF/env-entries.properties
        for (final EjbModule module : appModule.getEjbModules()) {
            for (final Map.Entry<String, String> entry : getEnvEntries(module).entrySet()) {
                final EnvEntry envEntry = new EnvEntry(entry.getKey(), "java.lang.String", entry.getValue());

                // To override a specific ejb only
                // the following format is used:
                // <ejb-name>.name = value
                if (envEntry.getName().contains(".")) {
                    String name = envEntry.getName();
                    final String ejbName = name.substring(0, name.indexOf('.'));
                    name = name.substring(name.indexOf('.') + 1);
                    final EnterpriseBean bean = module.getEjbJar().getEnterpriseBean(ejbName);
                    if (bean != null) {
                        // Set the new property name without the <ejb-name>. prefix
                        envEntry.setName(name);
                        apply(bean, envEntry, bean.getEjbName());
                        continue;
                    }
                }

                for (final EnterpriseBean bean : module.getEjbJar().getEnterpriseBeans()) {
                    apply(bean, envEntry, bean.getEjbName());
                }
            }
        }

        return appModule;
    }

    public WebModule deploy(final WebModule webModule) {
        for (final Map.Entry<String, String> entry : getEnvEntries(webModule).entrySet()) {
            final EnvEntry envEntry = new EnvEntry(entry.getKey(), "java.lang.String", entry.getValue());
            apply(webModule.getWebApp(), envEntry, "WebApp");
        }
        return webModule;
    }

    private void apply(final JndiConsumer bean, final EnvEntry newEntry, final String componentName) {
        EnvEntry entry = bean.getEnvEntryMap().get(newEntry.getName());
        if (entry == null) {
            entry = bean.getEnvEntryMap().get("java:comp/env/" + newEntry.getName());
        }
        if (entry != null) {
            if (SystemInstance.get().getOptions().get("envprops.override", false)) {
                log.debug("envprops.override", componentName, entry.getName(), entry.getEnvEntryValue(), newEntry.getEnvEntryValue());
                entry.setEnvEntryValue(newEntry.getEnvEntryValue());
            }
            return;
        }

        // Must not be an override, just add the new entry
        log.debug("envprops.add", componentName, newEntry.getName(), newEntry.getEnvEntryValue());
        bean.getEnvEntry().add(newEntry);
    }

    @SuppressWarnings({"unchecked"})
    private Map<String, String> getEnvEntries(final DeploymentModule module) {
        final Object value = module.getAltDDs().get(descriptorName);
        if (value instanceof Map) {
            return (Map<String, String>) value;
        }

        URL propsUrl = (URL) value;
        if (propsUrl == null) {
            propsUrl = (URL) module.getAltDDs().get(descriptorName);
        }
        if (propsUrl == null) {
            return Collections.emptyMap();
        }
        try {
            final Properties envEntriesProps = IO.readProperties(propsUrl);
            envEntriesProps.putAll(additionalEnvEntries);
            return new HashMap(envEntriesProps);
        } catch (final IOException e) {
            log.error("envprops.notLoaded", e, module.getModuleId(), propsUrl.toExternalForm());
            return Collections.emptyMap();
        }
    }

    public void addEnvEntries(final String key, final String value) {
        additionalEnvEntries.put(key, value);
    }

    public void resetAdditionalEnvEntries() {
        additionalEnvEntries.clear();
    }
}
