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

import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.SuperProperties;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AppModule implements DeploymentModule {
    public static final boolean DELEGATE_FIRST_DEFAULT = SystemInstance.get().getOptions().get("openejb.classloader.delegate-first", ParentClassLoaderFinder.Helper.get() != ClassLoader.getSystemClassLoader());

    private final Properties properties = new SuperProperties().caseInsensitive(true);
    private final Application application;
    private final ValidationContext validation;
    private final List<URL> additionalLibraries = new ArrayList<>();
    private final List<URL> scannableContainerUrls = new ArrayList<>();
    private final List<ConnectorModule> connectorModules = new ArrayList<>();
    private final List<WebModule> webModules = new ArrayList<>();
    private final List<ClientModule> clientModules = new ArrayList<>();
    private final List<EjbModule> ejbModules = new ArrayList<>();
    private final List<PersistenceModule> persistenceModules = new ArrayList<>();
    private final Map<String, TransactionType> txTypeByUnit = new HashMap<>();
    // TODO We could turn this into the Resources JAXB object and support containers and other things as well
    private final Collection<Resource> resources = new LinkedHashSet<>();
    private final Collection<Container> containers = new HashSet<>();
    private final Collection<Service> services = new HashSet<>();
    private final ClassLoader classLoader;
    private EntityMappings cmpMappings;
    private final Map<String, Object> altDDs = new HashMap<>();
    private final Set<String> watchedResources = new TreeSet<>();
    private final boolean standaloneModule;
    private boolean delegateFirst = DELEGATE_FIRST_DEFAULT;
    private final Set<String> additionalLibMbeans = new TreeSet<>();
    private final Collection<String> jaxRsProviders = new TreeSet<>();
    private final Map<String, PojoConfiguration> pojoConfigurations = new HashMap<>();
    private IAnnotationFinder earLibFinder;

    private ID id;
    private boolean webapp;

    public AppModule(final ClassLoader classLoader, final String jarLocation) {
        this(classLoader, jarLocation, null, false);
    }

    public List<URL> getScannableContainerUrls() {
        return scannableContainerUrls;
    }

    // shared between org.apache.openejb.config.AutoConfig.resolvePersistenceRefs() and org.apache.openejb.config.AppInfoBuilder.buildPersistenceModules()
    public String persistenceUnitId(final String rootUrl, final String name) {
        return name + " " + rootUrl.hashCode() + uniqueHostIfExists();
    }

    public String uniqueHostIfExists() {
        final boolean hasWebApps = !getWebModules().isEmpty();
        if (isWebapp() && hasWebApps) {
            return getWebModules().iterator().next().getHost();
        } else if (hasWebApps) {
            String id = null;
            for (final WebModule web : getWebModules()) {
                if (id == null) {
                    id = web.getHost();
                } else if (!id.equals(web.getHost())) {
                    return ""; // find something better as in org.apache.openejb.config.InitEjbDeployments
                }
            }
            return id;
        }
        return "";
    }

    public <T extends DeploymentModule> AppModule(final T... modules) {
        final T firstModule = modules[0];

        this.standaloneModule = true;
        this.classLoader = firstModule.getClassLoader();
        this.application = new Application(firstModule.getModuleId());

        this.id = new ID(null, application, null, firstModule.getFile(), firstModule.getModuleUri(), this);
        this.validation = new ValidationContext(this);

        for (final T module : modules) {
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
                addPersistenceModule((PersistenceModule) module);
            } else {
                throw new IllegalArgumentException("Unknown module type: " + type.getName());
            }
        }
    }

    public boolean isDelegateFirst() {
        return delegateFirst;
    }

    public void setDelegateFirst(final boolean delegateFirst) {
        this.delegateFirst = delegateFirst;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public AppModule appModule() {
        return this;
    }

    public AppModule(final ClassLoader classLoader, final String jarLocation, final Application application, final boolean standaloneModule) {
        this.classLoader = classLoader;
        this.application = application;

        final File file = jarLocation == null ? null : new File(jarLocation);
        this.id = new ID(null, application, null, file, null, this);
        this.validation = new ValidationContext(this);
        this.standaloneModule = standaloneModule;
    }

    public Set<String> getAdditionalLibMbeans() {
        return additionalLibMbeans;
    }

    @Override
    public boolean isStandaloneModule() {
        return standaloneModule;
    }

    @Override
    public void setStandaloneModule(final boolean isStandalone) {
        //do nothing
    }

    @Override
    public ValidationContext getValidation() {
        return validation;
    }

    public boolean hasWarnings() {
        if (validation.hasWarnings()) {
            return true;
        }
        for (final EjbModule module : ejbModules) {
            if (module.getValidation().hasWarnings()) {
                return true;
            }
        }
        for (final ClientModule module : clientModules) {
            if (module.getValidation().hasWarnings()) {
                return true;
            }
        }
        for (final ConnectorModule module : connectorModules) {
            if (module.getValidation().hasWarnings()) {
                return true;
            }
        }
        for (final WebModule module : webModules) {
            if (module.getValidation().hasWarnings()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFailures() {
        if (validation.hasFailures()) {
            return true;
        }
        for (final EjbModule module : ejbModules) {
            if (module.getValidation().hasFailures()) {
                return true;
            }
        }
        for (final ClientModule module : clientModules) {
            if (module.getValidation().hasFailures()) {
                return true;
            }
        }
        for (final ConnectorModule module : connectorModules) {
            if (module.getValidation().hasFailures()) {
                return true;
            }
        }
        for (final WebModule module : webModules) {
            if (module.getValidation().hasFailures()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasErrors() {
        if (validation.hasErrors()) {
            return true;
        }
        for (final EjbModule module : ejbModules) {
            if (module.getValidation().hasErrors()) {
                return true;
            }
        }
        for (final ClientModule module : clientModules) {
            if (module.getValidation().hasErrors()) {
                return true;
            }
        }
        for (final ConnectorModule module : connectorModules) {
            if (module.getValidation().hasErrors()) {
                return true;
            }
        }
        for (final WebModule module : webModules) {
            if (module.getValidation().hasErrors()) {
                return true;
            }
        }
        return false;
    }

    public List<ValidationContext> getValidationContexts() {
        final List<ValidationContext> contexts = new ArrayList<>();

        contexts.add(getValidation());

        for (final EjbModule module : ejbModules) {
            contexts.add(module.getValidation());
        }
        for (final ClientModule module : clientModules) {
            contexts.add(module.getValidation());
        }
        for (final ConnectorModule module : connectorModules) {
            contexts.add(module.getValidation());
        }
        for (final WebModule module : webModules) {
            contexts.add(module.getValidation());
        }
        return contexts;
    }

    @Override
    public String getJarLocation() {
        return id.getLocation() != null ? id.getLocation().getAbsolutePath() : null;
    }

    public void setModuleId(final String moduleId) {

        this.id = new ID(null, application, moduleId, id.getLocation(), id.getUri(), this);
    }

    @Override
    public String getModuleId() {
        return id.getName();
    }

    @Override
    public File getFile() {
        return id.getLocation();
    }

    @Override
    public URI getModuleUri() {
        return id.getUri();
    }

    @Override
    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    @Override
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

    public List<URL> getAdditionalLibraries() {
        return additionalLibraries;
    }

    public EntityMappings getCmpMappings() {
        return cmpMappings;
    }

    public void setCmpMappings(final EntityMappings cmpMappings) {
        this.cmpMappings = cmpMappings;
    }

    public List<ConnectorModule> getConnectorModules() {
        return connectorModules;
    }

    public List<WebModule> getWebModules() {
        return webModules;
    }

    @Override
    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public Collection<Container> getContainers() {
        return containers;
    }

    public Collection<Service> getServices() {
        return services;
    }

    public Collection<DeploymentModule> getDeploymentModule() {
        final ArrayList<DeploymentModule> modules = new ArrayList<>();
        modules.addAll(ejbModules);
        modules.addAll(webModules);
        modules.addAll(connectorModules);
        modules.addAll(clientModules);
        return modules;
    }

    @Override
    public String toString() {
        return "AppModule{" +
            "moduleId='" + id.getName() + '\'' +
            '}';
    }

    public void setStandloneWebModule() {
        webapp = true;
    }

    public boolean isWebapp() {
        return webapp;
    }

    public Collection<String> getJaxRsProviders() {
        return jaxRsProviders;
    }

    public void addPersistenceModule(final PersistenceModule root) {
        persistenceModules.add(root);

        final Persistence persistence = root.getPersistence();
        for (final PersistenceUnit unit : persistence.getPersistenceUnit()) {
            txTypeByUnit.put(unit.getName(), unit.getTransactionType());
        }
    }

    public void addPersistenceModules(final Collection<PersistenceModule> roots) {
        for (final PersistenceModule root : roots) {
            addPersistenceModule(root);
        }
    }

    public TransactionType getTransactionType(final String unit) {
        if (unit == null || unit.isEmpty()) {
            if (txTypeByUnit.size() == 1) {
                return txTypeByUnit.values().iterator().next();
            }
        }

        TransactionType type = txTypeByUnit.get(unit);
        if (type == null) { // default, shouldn't occur
            type = TransactionType.JTA;
        }
        return type;
    }

    public Map<String, PojoConfiguration> getPojoConfigurations() {
        return pojoConfigurations;
    }

    public IAnnotationFinder getEarLibFinder() {
        return earLibFinder;
    }

    public void setEarLibFinder(final IAnnotationFinder earLibFinder) {
        this.earLibFinder = earLibFinder;
    }
}
