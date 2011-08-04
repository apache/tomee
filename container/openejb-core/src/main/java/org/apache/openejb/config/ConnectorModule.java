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

import org.apache.openejb.jee.Connector;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorModule extends Module implements DeploymentModule {
    private final ValidationContext validation;

    private Connector connector;
    private ClassLoader classLoader;
    private final List<URL> libraries = new ArrayList<URL>();
    private final Set<String> watchedResources = new TreeSet<String>();

    private ID id;

	private IAnnotationFinder finder;
    
    public ConnectorModule(Connector connector) {
        this(connector, Thread.currentThread().getContextClassLoader(), null, null);
    }

    public ConnectorModule(Connector connector, ClassLoader classLoader, String jarLocation, String moduleId) {
        this.connector = connector;
        this.classLoader = classLoader;

        File file = (jarLocation == null) ? null : new File(jarLocation);
        this.id = new ID(null, connector, moduleId, file, null, this);
        this.validation = new ValidationContext(this);
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public String getJarLocation() {
        return (id.getLocation() != null) ? id.getLocation().getAbsolutePath() : null;
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

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<URL> getLibraries() {
        return libraries;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    @Override
    public String toString() {
        return "ConnectorModule{" +
                "moduleId='" + id.getName() + '\'' +
                '}';
    }

	public IAnnotationFinder getFinder() {
		return finder;
	}

	public void setFinder(IAnnotationFinder finder) {
		this.finder = finder;
	}
}
