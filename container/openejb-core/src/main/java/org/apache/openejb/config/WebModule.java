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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.Webservices;

/**
 * @version $Rev$ $Date$
 */
public class WebModule implements WsModule {
    private final ValidationContext validation;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();

    private WebApp webApp;
    private Webservices webservices;
    private String host;
    private String contextRoot;
    private ClassLoader classLoader;
    private String jarLocation;
    private final String moduleId;
    private final List<TldTaglib> taglibs = new ArrayList<TldTaglib>();
    private final Set<String> watchedResources = new TreeSet<String>();
    // List of all faces configuration files found in this web module
    private final List<FacesConfig> facesConfigs = new ArrayList<FacesConfig>();

    public WebModule(WebApp webApp, String contextRoot, ClassLoader classLoader, String jarLocation, String moduleId) {
        this.webApp = webApp;
        if (contextRoot == null) {
            contextRoot = jarLocation.substring(jarLocation.lastIndexOf(System.getProperty("file.separator")));
            if (contextRoot.endsWith(".unpacked")) {
                contextRoot = contextRoot.substring(0, contextRoot.length() - ".unpacked".length());
            }
            if (contextRoot.endsWith(".war")) {
                contextRoot = contextRoot.substring(0, contextRoot.length() - ".war".length());
            }
        }
        if (contextRoot.startsWith("/")) contextRoot = contextRoot.substring(1);
        this.contextRoot = contextRoot;
        this.classLoader = classLoader;
        this.jarLocation = jarLocation;

        if (webApp != null) webApp.setContextRoot(contextRoot);

        if (moduleId == null){
            if (webApp != null && webApp.getId() != null){
                moduleId = webApp.getId();
            } else {
                File file = new File(jarLocation);
                moduleId = file.getName();
                if (moduleId.endsWith(".unpacked")) {
                    moduleId = moduleId.substring(0, moduleId.length() - ".unpacked".length());
                }
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
        if (webApp != null) webApp.setContextRoot(contextRoot);
    }

    public Webservices getWebservices() {
        return webservices;
    }

    public void setWebservices(Webservices webservices) {
        this.webservices = webservices;
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
        if (webApp != null) webApp.setContextRoot(contextRoot);
        this.contextRoot = contextRoot;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public List<TldTaglib> getTaglibs() {
        return taglibs;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }
    public List<FacesConfig> getFacesConfigs() {
		return facesConfigs;
	}
}
