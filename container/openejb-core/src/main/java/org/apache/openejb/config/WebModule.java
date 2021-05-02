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

import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.IAnnotationFinder;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class WebModule extends Module implements WsModule, RESTModule {

    private final ValidationContext validation;

    private WebApp webApp;
    private Webservices webservices;
    private String host;
    private String contextRoot;
    private String defaultContextPath;
    private final List<TldTaglib> taglibs = new ArrayList<>();
    private final Set<String> watchedResources = new TreeSet<>();
    // List of all faces configuration files found in this web module
    private final List<FacesConfig> facesConfigs = new ArrayList<>();
    private IAnnotationFinder finder;
    private final Set<String> restClasses = new TreeSet<>();
    private final Set<String> ejbWebServices = new TreeSet<>();
    private final Set<String> ejbRestServices = new TreeSet<>();
    private final Set<String> jaxrsProviders = new TreeSet<>();
    private final Set<String> restApplications = new TreeSet<>();
    private final Map<String, Set<String>> jsfAnnotatedClasses = new HashMap<>();
    private final Map<String, Set<String>> webAnnotatedClasses = new HashMap<>();

    private final ID id;

    // keep the list of filtered URL we got after applying include/exclude pattern (@See DeploymentsResolver.loadFromClasspath)
    private List<URL> urls;
    private List<URL> rarUrls;
    private List<URL> addedUrls;
    private List<URL> scannableUrls;

    public WebModule(final WebApp webApp, String contextRoot, final ClassLoader classLoader, final String jarLocation, final String moduleId) {
        this.webApp = webApp;

        final File file = jarLocation == null ? null : new File(jarLocation);
        this.id = new ID(null, webApp, moduleId, file, null, this);
        this.validation = new ValidationContext(this);

        if (contextRoot == null) {

            contextRoot = null != jarLocation ? jarLocation.substring(jarLocation.lastIndexOf(File.separator)) : ".";

            if (contextRoot.endsWith(".unpacked")) {
                contextRoot = contextRoot.substring(0, contextRoot.length() - ".unpacked".length());
            }
            if (contextRoot.endsWith(".war")) {
                contextRoot = contextRoot.substring(0, contextRoot.length() - ".war".length());
            }
        }

        while (contextRoot.startsWith("/")) {
            contextRoot = contextRoot.substring(1);
        }

        while (contextRoot.startsWith("\\")) {
            contextRoot = contextRoot.substring(1);
        }

        this.contextRoot = contextRoot;
        setClassLoader(classLoader);

        if (webApp != null) {
            webApp.setContextRoot(contextRoot);
        }

        host = SystemInstance.get().getProperty(id.getName() + ".host", null);
    }

    @Override
    public String getJarLocation() {
        return id.getLocation() != null ? id.getLocation().getAbsolutePath() : null;
    }

    @Override
    public String getModuleId() {
        return id.getName();
    }

    @Override
    public File getFile() {
        return id.getLocation();
    }

    public String getDefaultContextPath() {
        return defaultContextPath;
    }

    public void setDefaultContextPath(final String defaultContextPath) {
        this.defaultContextPath = defaultContextPath;
    }

    @Override
    public URI getModuleUri() {
        return id.getUri();
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(final List<URL> urls) {
        this.urls = urls;
    }

    public IAnnotationFinder getFinder() {
        return finder;
    }

    public void setFinder(final IAnnotationFinder finder) {
        this.finder = finder;
    }

    @Override
    public ValidationContext getValidation() {
        return validation;
    }

    public WebApp getWebApp() {
        return webApp;
    }

    public void setWebApp(final WebApp webApp) {
        this.webApp = webApp;
        if (webApp != null) {
            webApp.setContextRoot(contextRoot);
        }
    }

    @Override
    public Webservices getWebservices() {
        return webservices;
    }

    @Override
    public void setWebservices(final Webservices webservices) {
        this.webservices = webservices;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(final String contextRoot) {
        if (webApp != null) {
            webApp.setContextRoot(contextRoot);
        }
        this.contextRoot = contextRoot;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public List<TldTaglib> getTaglibs() {
        return taglibs;
    }

    @Override
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

    @Override
    public Set<String> getRestClasses() {
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

    public List<URL> getScannableUrls() {
        if (scannableUrls == null) {
            return Collections.emptyList();
        }
        return scannableUrls;
    }

    public void setScannableUrls(final List<URL> scannableUrls) {
        this.scannableUrls = scannableUrls;
    }

    public Map<String, Set<String>> getJsfAnnotatedClasses() {
        return jsfAnnotatedClasses;
    }

    public Map<String, Set<String>> getWebAnnotatedClasses() {
        return webAnnotatedClasses;
    }

    public Set<String> getJaxrsProviders() {
        return jaxrsProviders;
    }

    public List<URL> getRarUrls() {
        if (rarUrls == null) {
            return Collections.emptyList();
        }
        return rarUrls;
    }

    public void setRarUrls(final List<URL> rarUrls) {
        this.rarUrls = rarUrls;
    }

    public List<URL> getAddedUrls() {
        return addedUrls;
    }

    public void setAddedUrls(final List<URL> addedUrls) {
        this.addedUrls = addedUrls;
    }

    @Override
    public AppModule appModule() {
        return super.getAppModule();
    }
}
