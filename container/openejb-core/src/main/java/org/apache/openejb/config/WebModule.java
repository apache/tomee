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
import java.net.URI;
import java.net.URL;
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
import org.apache.xbean.finder.IAnnotationFinder;

/**
 * @version $Rev$ $Date$
 */
public class WebModule extends Module implements WsModule, RESTModule {
    private final ValidationContext validation;

    private WebApp webApp;
    private Webservices webservices;
    private String host;
    private String contextRoot;
    private ClassLoader classLoader;
    private final List<TldTaglib> taglibs = new ArrayList<TldTaglib>();
    private final Set<String> watchedResources = new TreeSet<String>();
    // List of all faces configuration files found in this web module
    private final List<FacesConfig> facesConfigs = new ArrayList<FacesConfig>();
    private IAnnotationFinder finder;
    private final Set<String> restClasses = new TreeSet<String>();
    private final Set<String> ejbWebServices = new TreeSet<String>();
    private final Set<String> ejbRestServices = new TreeSet<String>();
    private final Set<String> restApplications = new TreeSet<String>();

    private ID id;
    
    // keep the list of filtered URL we got after applying include/exclude pattern (@See DeploymentsResolver.loadFromClasspath)
    private List<URL> urls;

    public WebModule(WebApp webApp, String contextRoot, ClassLoader classLoader, String jarLocation, String moduleId) {
        this.webApp = webApp;

        File file = (jarLocation == null) ? null : new File(jarLocation);
        this.id = new ID(null, webApp, moduleId, file, null, this);
        this.validation = new ValidationContext(this);

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

        if (webApp != null) webApp.setContextRoot(contextRoot);
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

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public IAnnotationFinder getFinder() {
        return finder;
    }

    public void setFinder(IAnnotationFinder finder) {
        this.finder = finder;
    }

    public ValidationContext getValidation() {
        return validation;
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

    @Override
    public String toString() {
        return "WebModule{" +
                "moduleId='" + id.getName() + '\'' +
                ", contextRoot='" + contextRoot + '\'' +
                '}';
    }

    @Override public Set<String> getRestClasses() {
        return restClasses;
    }

    public Set<String> getRestApplications() {
        return restApplications;
    }

    public Set<String> getEjbWebServices() {
        return ejbWebServices;
    }

    public Set<String> getEjbRestServices() {
        return ejbRestServices;
    }
}
