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

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.jpa.EntityMappings;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AppModule implements DeploymentModule {

    private final Application application;
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
    private final boolean standaloneModule;

    public AppModule(ClassLoader classLoader, String jarLocation) {
        this(classLoader, jarLocation, null, false);
    }

    public <T extends DeploymentModule> AppModule(T module) {
        this(module.getClassLoader(), module.getJarLocation(), new Application(module.getModuleId()), true);
        final Class<? extends DeploymentModule> type = module.getClass();

        if (type == EjbModule.class) {
            getEjbModules().add((EjbModule) module);
        } else if (type == ClientModule.class) {
            getClientModules().add((ClientModule) module);
        } else if (type == ConnectorModule.class) {
            getConnectorModules().add((ConnectorModule) module);
        } else if (type == WebModule.class) {
            getWebModules().add((WebModule) module);
        } else if (type == PersistenceModule.class) {
            getPersistenceModules().add((PersistenceModule) module);
        } else {
            throw new IllegalArgumentException("Unknown module type: " + type.getName());
        }
    }

    public AppModule(ClassLoader classLoader, String jarLocation, Application application, boolean standaloneModule) {
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;
        this.application = application;
        if (application == null || application.getApplicationName() == null) {
            String appId = null;
            if (jarLocation != null) {
                File file = new File(jarLocation);
                appId = file.getName();
                if (file.isFile() && appId.endsWith(".ear")) {
                    appId = appId.substring(0, appId.length() - 4);
                }
            }
            this.moduleId = appId;
        } else {
            this.moduleId = application.getApplicationName();
        }
        if (this.jarLocation == null) throw new IllegalArgumentException("jarLocation cannot be null");
        if (this.moduleId == null) throw new IllegalArgumentException("moduleId cannot be null");

        this.validation = new ValidationContext(AppModule.class, jarLocation);
        this.standaloneModule = standaloneModule;
    }

    public boolean isStandaloneModule() {
        return standaloneModule;
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

    public List<ValidationContext> getValidationContexts() {
        List<ValidationContext> contexts = new ArrayList<ValidationContext>();

        contexts.add(getValidation());

        for (EjbModule module : ejbModules) {
            contexts.add(module.getValidation());
        }
        for (ClientModule module : clientModules) {
            contexts.add(module.getValidation());
        }
        for (ConnectorModule module : connectorModules) {
            contexts.add(module.getValidation());
        }
        for (WebModule module : webModules) {
            contexts.add(module.getValidation());
        }
        return contexts;
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

    public Application getApplication() {
        return application;
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

    public List<ConnectorModule> getConnectorModules() {
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
