/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.NetworkUtil;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.recipe.ObjectRecipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class Configuration {

    private int httpPort = 8080;
    private int stopPort = 8005;
    private String host = "localhost";
    protected String dir;
    private File serverXml;
    private boolean keepServerXmlAsThis;
    private Properties properties;
    private boolean quickSession = true;
    private boolean skipHttp;

    private int httpsPort = 8443;
    private boolean ssl;
    private boolean withEjbRemote;
    private String keystoreFile;
    private String keystorePass;
    private String keystoreType = "JKS";
    private String clientAuth;
    private String keyAlias;
    private String sslProtocol;

    private String webXml;
    private LoginConfigBuilder loginConfig;
    private Collection<SecurityConstaintBuilder> securityConstraints = new LinkedList<>();
    private Collection<String> customWebResources = new LinkedList<>();

    private Realm realm;

    private boolean deployOpenEjbApp;

    private Map<String, String> users;
    private Map<String, String> roles;

    private boolean http2;

    private Filter classesFilter;

    private final Collection<Connector> connectors = new ArrayList<>();

    /**
     * when needed temp file only (deployClasspathAsWebapp() for instance)
     */
    private String tempDir = new File(System.getProperty("java.io.tmpdir"), "tomee-embedded_" + System.currentTimeMillis()).getAbsolutePath();

    private boolean webResourceCached = true;

    private String conf;
    private boolean deleteBaseOnStartup = true;

    public Configuration loadFrom(final String resource) {
        try (final InputStream is = findStream(resource)) {
            final Properties config = IO.readProperties(is, new Properties());
            loadFromProperties(config);
            return this;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private InputStream findStream(final String resource) throws FileNotFoundException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            final File file = new File(resource);
            if (file.exists()) {
                return new FileInputStream(file);
            } else {
                throw new IllegalArgumentException("Didn't find: " + resource);
            }
        }
        return stream;
    }

    public void loadFromProperties(final Properties config) {
        // filtering properties with system properties or themself
        final StrSubstitutor strSubstitutor = new StrSubstitutor(new StrLookup<String>() {
            @Override
            public String lookup(final String key) {
                final String property = System.getProperty(key);
                return property == null ? config.getProperty(key) : null;
            }
        });
        for (final String key : config.stringPropertyNames()) {
            final String val = config.getProperty(key);
            if (val == null || val.trim().isEmpty()) {
                continue;
            }
            final String newVal = strSubstitutor.replace(config.getProperty(key));
            if (!val.equals(newVal)) {
                config.setProperty(key, newVal);
            }
        }


        final String http = config.getProperty("http");
        if (http != null) {
            setHttpPort(Integer.parseInt(http));
        }
        final String https = config.getProperty("https");
        if (https != null) {
            setHttpsPort(Integer.parseInt(https));
        }
        final String stop = config.getProperty("stop");
        if (stop != null) {
            setStopPort(Integer.parseInt(stop));
        }
        final String host = config.getProperty("host");
        if (host != null) {
            setHost(host);
        }
        final String dir = config.getProperty("dir");
        if (dir != null) {
            setDir(dir);
        }
        final String serverXml = config.getProperty("serverXml");
        if (serverXml != null) {
            setServerXml(serverXml);
        }
        final String keepServerXmlAsThis = config.getProperty("keepServerXmlAsThis");
        if (keepServerXmlAsThis != null) {
            setKeepServerXmlAsThis(Boolean.parseBoolean(keepServerXmlAsThis));
        }
        final String quickSession = config.getProperty("quickSession");
        if (quickSession != null) {
            setQuickSession(Boolean.parseBoolean(quickSession));
        }
        final String skipHttp = config.getProperty("skipHttp");
        if (skipHttp != null) {
            setSkipHttp(Boolean.parseBoolean(skipHttp));
        }
        final String ssl = config.getProperty("ssl");
        if (ssl != null) {
            setSsl(Boolean.parseBoolean(ssl));
        }
        final String http2 = config.getProperty("http2");
        if (http2 != null) {
            setHttp2(Boolean.parseBoolean(http2));
        }
        final String deleteBaseOnStartup = config.getProperty("deleteBaseOnStartup");
        if (deleteBaseOnStartup != null) {
            setDeleteBaseOnStartup(Boolean.parseBoolean(deleteBaseOnStartup));
        }
        final String webResourceCached = config.getProperty("webResourceCached");
        if (webResourceCached != null) {
            setWebResourceCached(Boolean.parseBoolean(webResourceCached));
        }
        final String withEjbRemote = config.getProperty("withEjbRemote");
        if (withEjbRemote != null) {
            setWithEjbRemote(Boolean.parseBoolean(withEjbRemote));
        }
        final String deployOpenEjbApp = config.getProperty("deployOpenEjbApp");
        if (deployOpenEjbApp != null) {
            setDeployOpenEjbApp(Boolean.parseBoolean(deployOpenEjbApp));
        }
        final String keystoreFile = config.getProperty("keystoreFile");
        if (keystoreFile != null) {
            setKeystoreFile(keystoreFile);
        }
        final String keystorePass = config.getProperty("keystorePass");
        if (keystorePass != null) {
            setKeystorePass(keystorePass);
        }
        final String keystoreType = config.getProperty("keystoreType");
        if (keystoreType != null) {
            setKeystoreType(keystoreType);
        }
        final String clientAuth = config.getProperty("clientAuth");
        if (clientAuth != null) {
            setClientAuth(clientAuth);
        }
        final String keyAlias = config.getProperty("keyAlias");
        if (keyAlias != null) {
            setKeyAlias(keyAlias);
        }
        final String sslProtocol = config.getProperty("sslProtocol");
        if (sslProtocol != null) {
            setSslProtocol(sslProtocol);
        }
        final String webXml = config.getProperty("webXml");
        if (webXml != null) {
            setWebXml(webXml);
        }
        final String tempDir = config.getProperty("tempDir");
        if (tempDir != null) {
            setTempDir(tempDir);
        }
        final String customWebResources = config.getProperty("customWebResources");
        if (customWebResources != null) {
            setCustomWebResources(customWebResources);
        }
        final String classesFilterType = config.getProperty("classesFilter");
        if (classesFilterType != null) {
            try {
                setClassesFilter(Filter.class.cast(Thread.currentThread().getContextClassLoader().loadClass(classesFilterType).newInstance()));
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        final String conf = config.getProperty("conf");
        if (conf != null) {
            setConf(conf);
        }
        for (final String prop : config.stringPropertyNames()) {
            if (prop.startsWith("properties.")) {
                property(prop.substring("properties.".length()), config.getProperty(prop));
            } else if (prop.startsWith("users.")) {
                user(prop.substring("users.".length()), config.getProperty(prop));
            } else if (prop.startsWith("roles.")) {
                role(prop.substring("roles.".length()), config.getProperty(prop));
            } else if (prop.startsWith("connector.")) { // created in container
                property(prop, config.getProperty(prop));
            } else if (prop.equals("realm")) {
                final ObjectRecipe recipe = new ObjectRecipe(config.getProperty(prop));
                for (final String realmConfig : config.stringPropertyNames()) {
                    if (realmConfig.startsWith("realm.")) {
                        recipe.setProperty(realmConfig.substring("realm.".length()), config.getProperty(realmConfig));
                    }
                }
                setRealm(Realm.class.cast(recipe.create()));
            } else if (prop.equals("login")) {
                final ObjectRecipe recipe = new ObjectRecipe(LoginConfigBuilder.class.getName());
                for (final String nestedConfig : config.stringPropertyNames()) {
                    if (nestedConfig.startsWith("login.")) {
                        recipe.setProperty(nestedConfig.substring("login.".length()), config.getProperty(nestedConfig));
                    }
                }
                loginConfig(LoginConfigBuilder.class.cast(recipe.create()));
            } else if (prop.equals("securityConstraint")) {
                final ObjectRecipe recipe = new ObjectRecipe(SecurityConstaintBuilder.class.getName());
                for (final String nestedConfig : config.stringPropertyNames()) {
                    if (nestedConfig.startsWith("securityConstraint.")) {
                        recipe.setProperty(nestedConfig.substring("securityConstraint.".length()), config.getProperty(nestedConfig));
                    }
                }
                securityConstaint(SecurityConstaintBuilder.class.cast(recipe.create()));
            } else if (prop.equals("configurationCustomizer.")) {
                final String next = prop.substring("configurationCustomizer.".length());
                if (next.contains(".")) {
                    continue;
                }
                final ObjectRecipe recipe = new ObjectRecipe(properties.getProperty(prop + ".class"));
                for (final String nestedConfig : config.stringPropertyNames()) {
                    if (nestedConfig.startsWith(prop) && !prop.endsWith(".class")) {
                        recipe.setProperty(nestedConfig.substring(prop.length() + 1 /*dot*/), config.getProperty(nestedConfig));
                    }
                }
                addCustomizer(ConfigurationCustomizer.class.cast(recipe.create()));
            }
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public Configuration randomHttpPort() {
        this.httpPort = NetworkUtil.getNextAvailablePort();
        return this;
    }

    public int getStopPort() {
        return stopPort;
    }

    public void setStopPort(final int stopPort) {
        this.stopPort = stopPort;
    }

    public String getDir() {
        return dir;
    }

    public Configuration dir(final String dir) {
        setDir(dir);
        return this;
    }

    public boolean isWithEjbRemote() {
        return withEjbRemote;
    }

    public Configuration withEjbRemote(final boolean withEjbRemote) {
        setWithEjbRemote(withEjbRemote);
        return this;
    }

    public void setWithEjbRemote(final boolean withEjbRemote) {
        this.withEjbRemote = withEjbRemote;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public String getHost() {
        return host;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
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

    public void setServerXml(final String file) {
        if (file == null) {
            serverXml = null;
        } else {
            final File sXml = new File(file);
            if (sXml.exists()) {
                serverXml = sXml;
            }
        }
    }

    public File getServerXmlFile() {
        return serverXml;
    }

    public boolean hasServerXml() {
        return serverXml != null && serverXml.exists();
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isQuickSession() {
        return quickSession;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(final boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSkipHttp() {
        return skipHttp;
    }

    public void setSkipHttp(final boolean skipHttp) {
        this.skipHttp = skipHttp;
    }

    public void setQuickSession(final boolean quickSession) {
        this.quickSession = quickSession;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(final String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public Configuration property(final String key, final String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(key, value);
        return this;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(final String tempDir) {
        this.tempDir = tempDir;
    }

    public boolean isDeployOpenEjbApp() {
        return deployOpenEjbApp;
    }

    public void setDeployOpenEjbApp(final boolean deployOpenEjbApp) {
        this.deployOpenEjbApp = deployOpenEjbApp;
    }

    public Configuration http(final int port) {
        setHttpPort(port);
        return this;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(final Map<String, String> users) { // useful for tools like maven plugin
        this.users = users;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(final Map<String, String> roles) {
        this.roles = roles;
    }

    public boolean isKeepServerXmlAsThis() {
        return keepServerXmlAsThis;
    }

    public void setKeepServerXmlAsThis(final boolean keepServerXmlAsThis) {
        this.keepServerXmlAsThis = keepServerXmlAsThis;
    }

    public Configuration user(final String name, final String pwd) {
        if (users == null) {
            users = new HashMap<>();
        }
        this.users.put(name, pwd);
        return this;
    }

    public Configuration role(final String user, final String roles) {
        if (this.roles == null) {
            this.roles = new HashMap<>();
        }
        this.roles.put(user, roles);
        return this;
    }

    public Configuration setWebXml(final String webXml) {
        this.webXml = webXml;
        return this;
    }

    public String getWebXml() {
        return webXml;
    }

    public LoginConfigBuilder getLoginConfig() {
        return loginConfig;
    }

    public Configuration loginConfig(final LoginConfigBuilder loginConfig) {
        this.loginConfig = loginConfig;
        return this;
    }

    public Collection<SecurityConstaintBuilder> getSecurityConstraints() {
        return securityConstraints;
    }

    public Configuration securityConstaint(final SecurityConstaintBuilder constraint) {
        securityConstraints.add(constraint);
        return this;
    }

    public Realm getRealm() {
        return realm;
    }

    public Configuration setRealm(final Realm realm) {
        this.realm = realm;
        return this;
    }

    public boolean areWebResourcesCached() {
        return webResourceCached;
    }

    public void setWebResourceCached(boolean cached) {
        this.webResourceCached = cached;
    }

    public boolean isHttp2() {
        return http2;
    }

    public void setHttp2(final boolean http2) {
        this.http2 = http2;
    }

    public Collection<Connector> getConnectors() {
        return connectors;
    }

    public void addCustomizer(final ConfigurationCustomizer configurationCustomizer) {
        configurationCustomizer.customize(this);
    }

    public Configuration conf(final String config) {
        setConf(config);
        return this;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(final String conf) {
        this.conf = conf;
    }

    public void setClassesFilter(final Filter filter) {
        this.classesFilter = filter;
    }

    public Configuration classesFilter(final Filter filter) {
        setClassesFilter(filter);
        return this;
    }

    public Filter getClassesFilter() {
        return classesFilter;
    }

    public boolean isDeleteBaseOnStartup() {
        return deleteBaseOnStartup;
    }

    public void setDeleteBaseOnStartup(final boolean deleteBaseOnStartup) {
        this.deleteBaseOnStartup = deleteBaseOnStartup;
    }

    public void setCustomWebResources(final String web) {
        customWebResources.addAll(asList(web.split(",")));
    }

    public void addCustomWebResources(final String web) {
        customWebResources.add(web);
    }

    public Collection<String> getCustomWebResources() {
        return customWebResources;
    }

    public interface ConfigurationCustomizer {
        void customize(Configuration configuration);
    }
}
