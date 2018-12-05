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

import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.util.SuperProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Module {
    // note: 0 is reserved for internal app, don't set it to 0!
    private static int currentId = 1; // unique id to be able to bind something for each module in the jndi tree

    private boolean isStandaloneModule = true;

    private ClassLoader classLoader;

    private ValidationConfigType validationConfig;
    private final Map<String, Object> altDDs = new HashMap<>();
    private String uniqueId;
    private AppModule appModule;
    private Resources resources;
    private final Set<String> mbeans = new HashSet<>();
    private final Properties properties = new SuperProperties().caseInsensitive(true);

    public Module(final boolean needId) {
        if (needId) {
            uniqueId = Integer.toString(currentId++);
        }
    }

    public Module() {
        this(true);
    }

    public Properties getProperties() {
        return properties;
    }

    public ValidationConfigType getValidationConfig() {
        return validationConfig;
    }

    public void setValidationConfig(final ValidationConfigType v) {
        validationConfig = v;
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isStandaloneModule() {
        return isStandaloneModule;
    }

    public void setStandaloneModule(final boolean isStandalone) {
        isStandaloneModule = isStandalone;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void initAppModule(final AppModule appModule) {
        if (this.appModule == appModule) {
            return;
        }

        if (this.appModule != null) {
            throw new UnsupportedOperationException("AppModule is already set");
        }

        this.appModule = appModule;
        if (resources != null) {
            this.appModule.getResources().addAll(resources.getResource());
            this.appModule.getContainers().addAll(resources.getContainer());
            this.appModule.getServices().addAll(resources.getService());
        }
    }

    public AppModule getAppModule() {
        return appModule;
    }

    public void initResources(final Resources resources) {
        if (this.resources != null) {
            throw new UnsupportedOperationException("resources.xml is already set");
        }

        this.resources = resources;
    }

    public Set<String> getMbeans() {
        return mbeans;
    }
}
