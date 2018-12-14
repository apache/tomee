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

import org.apache.openejb.jee.ApplicationClient;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @version $Rev$ $Date$
 */
public class ClientModule extends Module implements DeploymentModule {
    private final ValidationContext validation;
    private ApplicationClient applicationClient;
    private String mainClass;
    private boolean ejbModuleGenerated;
    private AtomicReference<IAnnotationFinder> finder;
    private final Set<String> localClients = new HashSet<>();
    private final Set<String> remoteClients = new HashSet<>();
    private ID id;
    private final Set<String> watchedResources = new TreeSet<>();

    public ClientModule(final ApplicationClient applicationClient, final ClassLoader classLoader, final String jarLocation, final String mainClass, final String moduleId) {
        this.applicationClient = applicationClient;
        setClassLoader(classLoader);
        this.mainClass = mainClass;

        final File file = jarLocation == null ? null : new File(jarLocation);
        this.id = new ID(null, applicationClient, moduleId, file, null, this);
        this.validation = new ValidationContext(this);
    }

    public boolean isEjbModuleGenerated() {
        return ejbModuleGenerated;
    }

    public void setEjbModuleGenerated(final boolean ejbModuleGenerated) {
        this.ejbModuleGenerated = ejbModuleGenerated;
    }

    public IAnnotationFinder getFinder() {
        return finder != null ? finder.get() : null;
    }

    public void setFinderReference(final AtomicReference<IAnnotationFinder> finder) {
        this.finder = finder;
    }

    public AtomicReference<IAnnotationFinder> getFinderReference() {
        return this.finder;
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public String getJarLocation() {
        return id.getLocation() != null ? id.getLocation().getAbsolutePath() : null;
    }

    public void setJarLocation(final String jarLocation) {
        this.id = new ID(null, applicationClient, id.getName(), new File(jarLocation), id.getUri(), this);
    }

    public String getModuleId() {
        return id.getName();
    }

    public File getFile() {
        return id.getLocation();
    }

    public URI getModuleUri() {
        return id.getUri();
    }

    public ApplicationClient getApplicationClient() {
        return applicationClient;
    }

    public void setApplicationClient(final ApplicationClient applicationClient) {
        this.applicationClient = applicationClient;
    }

    public Set<String> getLocalClients() {
        return localClients;
    }

    public Set<String> getRemoteClients() {
        return remoteClients;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(final String mainClass) {
        this.mainClass = mainClass;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    @Override
    public String toString() {
        return "ClientModule{" +
            "moduleId='" + id.getName() + '\'' +
            ", mainClass='" + mainClass + '\'' +
            '}';
    }

    @Override
    public AppModule appModule() {
        return super.getAppModule();
    }

}
