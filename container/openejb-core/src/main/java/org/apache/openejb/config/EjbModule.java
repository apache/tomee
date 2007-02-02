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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Class is to remain "dumb" and should not have deployment logic added to it.
 * Class is intentionally not an interface as that would encourage "smart" implementations
 * @version $Revision$ $Date$
 */
public class EjbModule implements DeploymentModule {

    private ClassLoader classLoader;
    private String jarURI;
    private EjbJar ejbJar;
    private OpenejbJar openejbJar;
    private String moduleId;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();

    public EjbModule(ClassLoader classLoader, String moduleName, String jarURI, EjbJar ejbJar, OpenejbJar openejbJar) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader is null");
        }
        this.classLoader = classLoader;
        this.ejbJar = ejbJar;
        this.jarURI = jarURI;
        this.openejbJar = openejbJar;

        if (moduleName == null){
            File file = new File(jarURI);
            moduleId = file.getName();
        }
    }

    public EjbModule(ClassLoader classLoader, String jarURI, EjbJar ejbJar, OpenejbJar openejbJar) {
        this(classLoader, null, jarURI, ejbJar, openejbJar);
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

    public String getJarURI() {
        return jarURI;
    }

    public void setJarURI(String jarURI) {
        this.jarURI = jarURI;
    }

    public String getJarLocation() {
        return getJarURI();
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
}
