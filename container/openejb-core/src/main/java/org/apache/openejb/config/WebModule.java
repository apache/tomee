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

import org.apache.openejb.jee.WebApp;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class WebModule implements DeploymentModule {
    private final ValidationContext validation;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();

    private WebApp webApp;
    private String contextRoot;
    private ClassLoader classLoader;
    private String jarLocation;
    private final String moduleId;

    public WebModule(WebApp webApp, String contextRoot, ClassLoader classLoader, String jarLocation, String moduleId) {
        this.webApp = webApp;
        this.contextRoot = contextRoot;
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;

        if (moduleId == null){
            if (webApp != null && webApp.getId() != null){
                moduleId = webApp.getId();
            } else {
                File file = new File(jarLocation);
                moduleId = file.getName();
            }
        }

        this.moduleId = moduleId;
        validation = new ValidationContext(WebModule.class, jarLocation);
    }

    public ValidationContext getValidation() {
        return validation;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public WebApp getWebApp() {
        return webApp;
    }

    public void setWebApp(WebApp webApp) {
        this.webApp = webApp;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public void setJarLocation(String jarLocation) {
        this.jarLocation = jarLocation;
    }


    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
}
