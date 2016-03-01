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
package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.arquillian.common.IO;
import org.apache.openejb.arquillian.common.Prefixes;
import org.apache.openejb.arquillian.common.TomEEConfiguration;
import org.jboss.arquillian.config.descriptor.api.Multiline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@Prefixes({"tomee", "tomee.embedded"})
public class EmbeddedTomEEConfiguration extends TomEEConfiguration {
    private int httpsPort = 8443;
    private boolean ssl;
    private boolean withEjbRemote;
    private String keystoreFile;
    private String keystorePass;
    private String keystoreType = "JKS";
    private String clientAuth;
    private String keyAlias;
    private String sslProtocol;
    private String users;
    private String roles;
    private boolean webResourcesCached = true;

    public boolean isWebResourcesCached() {
        return webResourcesCached;
    }

    public void setWebResourcesCached(final boolean webResourcesCached) {
        this.webResourcesCached = webResourcesCached;
    }

    public boolean isWithEjbRemote() {
        return withEjbRemote;
    }

    public void setWithEjbRemote(final boolean withEjbRemote) {
        this.withEjbRemote = withEjbRemote;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(final boolean ssl) {
        this.ssl = ssl;
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

    public String getUsers() {
        return users;
    }

    public Properties getUsersAsProperties() {
        return toProperties(users);
    }

    @Multiline
    public void setUsers(final String users) {
        this.users = users;
    }

    public String getRoles() {
        return roles;
    }

    public Properties getRolesAsProperties() {
        return toProperties(roles);
    }

    @Multiline
    public void setRoles(final String roles) {
        this.roles = roles;
    }

    @Override
    public int[] portsAlreadySet() {
        final List<Integer> value = new ArrayList<Integer>();
        if (getStopPort() > 0) {
            value.add(getStopPort());
        }
        if (getHttpPort() > 0) {
            value.add(getHttpPort());
        }
        if (getHttpsPort() > 0) {
            value.add(getHttpsPort());
        }
        return toInts(value);
    }

    public Properties systemPropertiesAsProperties() {
        if (properties == null || properties.isEmpty()) {
            return new Properties();
        }

        final Properties properties = toProperties(this.properties);
        if (properties != null && isUnsafeEjbd() &&
            "*".equals(properties.getProperty("tomee.serialization.class.blacklist", "-").trim())) {

            properties.remove("tomee.serialization.class.blacklist");
            properties.put("tomee.serialization.class.whitelist", "*");
        }

        return properties;
    }

    private static Properties toProperties(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        final Properties properties = new Properties();
        final ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes());
        try {
            properties.load(bais);
        } catch (final IOException e) {
            throw new OpenEJBRuntimeException(e);
        } finally {
            try {
                IO.close(bais);
            } catch (final IOException ignored) {
                // no-op
            }
        }
        return properties;
    }
}
