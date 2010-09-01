/**
 *
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

import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class is to remain "dumb" and should not have deployment logic added to it.
 * Class is intentionally not an interface as that would encourage "smart" implementations
 * @version $Revision$ $Date$
 */
public class EjbModule implements WsModule {

    private final ValidationContext validation;

    private ClassLoader classLoader;
    private String jarLocation;
    private EjbJar ejbJar;
    private OpenejbJar openejbJar;
    private Webservices webservices;
    private String moduleId;
    private final AtomicReference<AbstractFinder> finder = new AtomicReference<AbstractFinder>();
    private final Map<String,Object> altDDs = new HashMap<String,Object>();
    private final Set<String> watchedResources = new TreeSet<String>();
    private Beans beans;

    private ClientModule clientModule;

    public EjbModule(EjbJar ejbJar){
        this(Thread.currentThread().getContextClassLoader(), null, ejbJar, null);
    }

    public EjbModule(EjbJar ejbJar, OpenejbJar openejbJar){
        this(Thread.currentThread().getContextClassLoader(), null, ejbJar, openejbJar);
    }

    public EjbModule(ClassLoader classLoader, String moduleId, String jarURI, EjbJar ejbJar, OpenejbJar openejbJar) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader is null");
        }
        this.classLoader = classLoader;
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;

        if (jarURI == null){
            if (moduleId != null){
                jarURI = moduleId;
            } else if (ejbJar.getId() != null && !ejbJar.getId().equals("")){
                jarURI = ejbJar.getId();
            } else {
                jarURI = ejbJar.toString();
            }
        }
        this.jarLocation = jarURI;

        if (moduleId == null){
            if (ejbJar != null && ejbJar.getId() != null && !ejbJar.getId().equals("")){
                moduleId = ejbJar.getId();
            } else {
                File file = new File(jarURI);
                moduleId = file.getName();
                if (moduleId == null){
                    moduleId = jarURI;
                } else if (moduleId.endsWith(".jar")) {
                    moduleId = moduleId.substring(0, moduleId.length() - ".jar".length() );
                }
            }
        }
        this.moduleId = moduleId;
        validation = new ValidationContext(EjbModule.class, jarLocation);
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

    public AbstractFinder getFinder() {
        return finder.get();
    }

    public void setFinder(AbstractFinder finder) {
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

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public EjbJar getEjbJar() {
        return ejbJar;
    }

    public void setEjbJar(EjbJar ejbJar) {
        this.ejbJar = ejbJar;
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public void setJarLocation(String jarLocation) {
        this.jarLocation = jarLocation;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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
}
