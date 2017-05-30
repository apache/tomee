/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.gradle.embedded;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TomEEEmbeddedExtension {
    public static final String NAME = "tomee-embedded";
    public static final String ALIAS = "tomeeembedded"; // easier in build.gradle cause no iphen

    // specific to the extension
    private boolean skipDefaultRepository = false;
    private String tomeeVersion;

    // shared with the task
    private Integer httpPort;
    private Integer httpsPort;
    private Integer ajpPort;
    private Integer stopPort;
    private String host;
    private String keystoreFile;
    private String keystorePass;
    private String keystoreType;
    private String clientAuth;
    private String keyAlias;
    private String sslProtocol;
    private File serverXml;
    private Boolean singleClassloader;
    private Boolean ssl;
    private Boolean withEjbRemote;
    private Boolean quickSession;
    private Boolean skipHttp;
    private Collection<String> applicationScopes;
    private Collection<String> classloaderFilteredPackages;
    private Boolean webResourceCached;
    private String context;
    private Map<String, String> containerProperties;
    private Boolean keepServerXmlAsThis;
    private Map<String, String> users;
    private Map<String, String> roles;
    private Boolean forceJspDevelopment;
    private String inlinedServerXml;
    private String inlinedTomEEXml;
    private File workDir;
    private List<File> modules;
    private List<String> customWebResources;
    private File docBase;
    private String dir;
    private String conf;

    public Boolean isSkipDefaultRepository() {
        return skipDefaultRepository;
    }

    public void setSkipDefaultRepository(final boolean skipDefaultRepository) {
        this.skipDefaultRepository = skipDefaultRepository;
    }

    public String getTomeeVersion() {
        return tomeeVersion;
    }

    public void setTomeeVersion(final String tomeeVersion) {
        this.tomeeVersion = tomeeVersion;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(final Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(final Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(final Integer ajpPort) {
        this.ajpPort = ajpPort;
    }

    public Integer getStopPort() {
        return stopPort;
    }

    public void setStopPort(final Integer stopPort) {
        this.stopPort = stopPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(final String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(final String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(final String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(final String clientAuth) {
        this.clientAuth = clientAuth;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(final String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(final String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public File getServerXml() {
        return serverXml;
    }

    public void setServerXml(final File serverXml) {
        this.serverXml = serverXml;
    }

    public Boolean getSingleClassloader() {
        return singleClassloader;
    }

    public void setSingleClassloader(final Boolean singleClassloader) {
        this.singleClassloader = singleClassloader;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(final Boolean ssl) {
        this.ssl = ssl;
    }

    public Boolean getWithEjbRemote() {
        return withEjbRemote;
    }

    public void setWithEjbRemote(final Boolean withEjbRemote) {
        this.withEjbRemote = withEjbRemote;
    }

    public Boolean getQuickSession() {
        return quickSession;
    }

    public void setQuickSession(final Boolean quickSession) {
        this.quickSession = quickSession;
    }

    public Boolean getSkipHttp() {
        return skipHttp;
    }

    public void setSkipHttp(final Boolean skipHttp) {
        this.skipHttp = skipHttp;
    }

    public Collection<String> getApplicationScopes() {
        return applicationScopes;
    }

    public void setApplicationScopes(final Collection<String> applicationScopes) {
        this.applicationScopes = applicationScopes;
    }

    public Collection<String> getClassloaderFilteredPackages() {
        return classloaderFilteredPackages;
    }

    public void setClassloaderFilteredPackages(final Collection<String> classloaderFilteredPackages) {
        this.classloaderFilteredPackages = classloaderFilteredPackages;
    }

    public Boolean getWebResourceCached() {
        return webResourceCached;
    }

    public void setWebResourceCached(final Boolean webResourceCached) {
        this.webResourceCached = webResourceCached;
    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public Map<String, String> getContainerProperties() {
        return containerProperties;
    }

    public void setContainerProperties(final Map<String, String> containerProperties) {
        this.containerProperties = containerProperties;
    }

    public Boolean getKeepServerXmlAsThis() {
        return keepServerXmlAsThis;
    }

    public void setKeepServerXmlAsThis(final Boolean keepServerXmlAsThis) {
        this.keepServerXmlAsThis = keepServerXmlAsThis;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(final Map<String, String> users) {
        this.users = users;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(final Map<String, String> roles) {
        this.roles = roles;
    }

    public Boolean getForceJspDevelopment() {
        return forceJspDevelopment;
    }

    public void setForceJspDevelopment(final Boolean forceJspDevelopment) {
        this.forceJspDevelopment = forceJspDevelopment;
    }

    public String getInlinedServerXml() {
        return inlinedServerXml;
    }

    public void setInlinedServerXml(final String inlinedServerXml) {
        this.inlinedServerXml = inlinedServerXml;
    }

    public String getInlinedTomEEXml() {
        return inlinedTomEEXml;
    }

    public void setInlinedTomEEXml(final String inlinedTomEEXml) {
        this.inlinedTomEEXml = inlinedTomEEXml;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(final File workDir) {
        this.workDir = workDir;
    }

    public List<File> getModules() {
        return modules;
    }

    public void setModules(final List<File> modules) {
        this.modules = modules;
    }

    public File getDocBase() {
        return docBase;
    }

    public void setDocBase(final File docBase) {
        this.docBase = docBase;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(final String conf) {
        this.conf = conf;
    }

    public List<String> getCustomWebResources() {
        return customWebResources;
    }

    public void setCustomWebResources(final List<String> customWebResources) {
        this.customWebResources = customWebResources;
    }
}
