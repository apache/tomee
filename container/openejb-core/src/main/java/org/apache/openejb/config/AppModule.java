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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;

import org.apache.openejb.jee.jpa.EntityMappings;

/**
 * @version $Rev$ $Date$
 */
public class AppModule implements DeploymentModule {

    private final ValidationContext validation;
    private final List<URL> additionalLibraries = new ArrayList<URL>();
    private final List<ConnectorModule> connectorModules = new ArrayList<ConnectorModule>();
    private final List<WebModule> webModules = new ArrayList<WebModule>();
    private final List<ClientModule> clientModules = new ArrayList<ClientModule>();
    private final List<EjbModule> ejbModules = new ArrayList<EjbModule>();
    private final List<PersistenceModule> persistenceModules = new ArrayList<PersistenceModule>();
    private final String jarLocation;
    private final ClassLoader classLoader;
    private EntityMappings cmpMappings;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();
    private final String moduleId;
    private final Set<String> watchedResources = new TreeSet<String>();

    public AppModule(ClassLoader classLoader, String jarLocation) {
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;
        File file = new File(jarLocation);
        moduleId = file.getName();
        validation = new ValidationContext(AppModule.class, jarLocation);
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public boolean hasWarnings() {
        if (validation.hasWarnings()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        return false;
    }

    public boolean hasFailures() {
        if (validation.hasFailures()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        return false;
    }

    public boolean hasErrors() {
        if (validation.hasErrors()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        return false;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<ClientModule> getClientModules() {
        return clientModules;
    }

    public List<EjbModule> getEjbModules() {
        return ejbModules;
    }

    public List<PersistenceModule> getPersistenceModules() {
        return persistenceModules;
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public List<URL> getAdditionalLibraries() {
        return additionalLibraries;
    }

    public EntityMappings getCmpMappings() {
        return cmpMappings;
    }

    public void setCmpMappings(EntityMappings cmpMappings) {
        this.cmpMappings = cmpMappings;
    }

    public List<ConnectorModule> getResourceModules() {
        return connectorModules;
    }

    public List<WebModule> getWebModules() {
        return webModules;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    public Collection<DeploymentModule> getDeploymentModule() {
        ArrayList<DeploymentModule> modules = new ArrayList<DeploymentModule>();
        modules.addAll(ejbModules);
        modules.addAll(webModules);
        modules.addAll(connectorModules);
        modules.addAll(clientModules);
        return modules;
    }
}
