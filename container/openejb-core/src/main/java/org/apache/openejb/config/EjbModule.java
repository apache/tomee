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

import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedModule;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class is to remain "dumb" and should not have deployment logic added to it.
 * Class is intentionally not an interface as that would encourage "smart" implementations
 *
 * @version $Revision$ $Date$
 */
public class EjbModule extends Module implements WsModule {

    private final ValidationContext validation;

    private EjbJar ejbJar;
    private OpenejbJar openejbJar;
    private Webservices webservices;

    private final AtomicReference<IAnnotationFinder> finder = new AtomicReference<IAnnotationFinder>();
    private final Set<String> watchedResources = new TreeSet<String>();
    private Beans beans;

    private ClientModule clientModule;
    private ID id;

    private final Set<String> repositories = new TreeSet<String>();

    public EjbModule(EjbJar ejbJar) {
        this(Thread.currentThread().getContextClassLoader(), null, ejbJar, null);
    }

    public EjbModule(EjbJar ejbJar, OpenejbJar openejbJar) {
        this(Thread.currentThread().getContextClassLoader(), null, ejbJar, openejbJar);
    }

    public EjbModule(ClassLoader classLoader, String moduleId, String jarURI, EjbJar ejbJar, OpenejbJar openejbJar) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader is null");
        }
        setClassLoader(classLoader);
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;

        File file = null;

        if (jarURI != null) file = new File(jarURI);

        this.id = new ID(openejbJar, ejbJar, moduleId, file, null, this);
        this.validation = new ValidationContext(this);
    }

    public EjbModule(ClassLoader classLoader, String jarURI, EjbJar ejbJar, OpenejbJar openejbJar) {
        this(classLoader, null, jarURI, ejbJar, openejbJar);
    }

    public Beans getBeans() {
        return beans;
    }

    public void setBeans(Beans beans) {
        this.beans = beans;
    }

    public IAnnotationFinder getFinder() {
        return finder.get();
    }

    public void setFinder(IAnnotationFinder finder) {
        this.finder.set(finder);
    }

    public ClientModule getClientModule() {
        return clientModule;
    }

    public void setClientModule(ClientModule clientModule) {
        this.clientModule = clientModule;
        if (clientModule != null) {
            clientModule.setEjbModuleGenerated(true);
            clientModule.setFinderReference(finder);
        }
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public EjbJar getEjbJar() {
        return ejbJar;
    }

    public void setEjbJar(EjbJar ejbJar) {
        this.ejbJar = ejbJar;
    }

    public String getJarLocation() {
        return (id.getLocation() != null) ? id.getLocation().getAbsolutePath() : null;
    }

    public void setJarLocation(String jarLocation) {
        this.id = new ID(openejbJar, ejbJar, id.getName(), new File(jarLocation), id.getUri(), this);
    }

    public String getModuleId() {
        return id.getName();
    }

    public File getFile() {
        return id.getLocation();
    }
    
    public void setModuleId(String moduleId) {
        if (openejbJar == null) openejbJar = new OpenejbJar();
        openejbJar.setModuleName(moduleId);
        
        this.id = new ID(openejbJar, ejbJar, moduleId, id.getLocation(), id.getUri(), this);
    }

    public URI getModuleUri() {
        return id.getUri();
    }

    public void setModuleUri(URI moduleUri) {
        this.id = new ID(openejbJar, ejbJar, id.getName(), id.getLocation(), moduleUri, this);
    }

    public OpenejbJar getOpenejbJar() {
        return openejbJar;
    }

    public void setOpenejbJar(OpenejbJar openejbJar) {
        this.openejbJar = openejbJar;
    }

    public Webservices getWebservices() {
        return webservices;
    }

    public void setWebservices(Webservices webservices) {
        this.webservices = webservices;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    public Set<String> getRepositories() {
        return repositories;
    }

    @Override
    public String toString() {
        return "EjbModule{" +
                "moduleId='" + id.getName() + '\'' +
                '}';
    }
}
