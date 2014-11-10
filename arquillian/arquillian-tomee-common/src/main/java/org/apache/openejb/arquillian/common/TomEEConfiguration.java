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
package org.apache.openejb.arquillian.common;


import org.jboss.arquillian.config.descriptor.api.Multiline;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("UnusedDeclaration")
public class TomEEConfiguration implements ContainerConfiguration {

    protected boolean exportConfAsSystemProperty;
    protected int httpsPort = 8443;
    protected int httpPort = 8080;
    protected int stopPort = 8005;
    protected String dir = System.getProperty("java.io.tmpdir") + "/arquillian-apache-tomee";
    protected String appWorkingDir = System.getProperty("java.io.tmpdir") + "/arquillian-tomee-app-working-dir";
    protected String host = "localhost";
    protected String stopHost = "localhost"; // generally localhost but host (http) can be different
    protected String stopCommand = "SHUTDOWN"; // default one - can be overriden in server.xml
    protected String serverXml;
    protected String portRange = ""; // only used if port < 0, empty means whatever, can be "1024-65535"
    protected String preloadClasses; // just a client classloader.loadClass(), value is comma separated qualified names. Useful with maven resolver for instance
    protected boolean quickSession = true;
    protected boolean unpackWars = true;

    protected String properties = "";
    protected String webContextToUseWithEars;
    protected boolean keepServerXmlAsThis;
    protected boolean singleDumpByArchiveName;
    protected Collection<String> singleDeploymentByArchiveName = Collections.emptyList();

    public boolean isUnpackWars() {
        return unpackWars;
    }

    public void setUnpackWars(final boolean unpackWars) {
        this.unpackWars = unpackWars;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
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

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public String getAppWorkingDir() {
        return appWorkingDir;
    }

    public void setAppWorkingDir(final String appWorkingDir) {
        this.appWorkingDir = appWorkingDir;
    }

    public void validate() throws ConfigurationException {
    }

    public boolean getExportConfAsSystemProperty() {
        return exportConfAsSystemProperty;
    }

    public void setExportConfAsSystemProperty(final boolean exportConfAsSystemProperty) {
        this.exportConfAsSystemProperty = exportConfAsSystemProperty;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getServerXml() {
        return serverXml;
    }

    public void setServerXml(final String serverXml) {
        this.serverXml = serverXml;
    }

    public String getProperties() {
        return properties;
    }

    @Multiline
    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public String systemProperties() {
        return properties.replaceAll("\n *", "\n");
    }

    public String getPortRange() {
        return portRange;
    }

    public void setPortRange(final String portRange) {
        this.portRange = portRange;
    }

    public boolean isQuickSession() {
        return quickSession;
    }

    public void setQuickSession(final boolean quickSession) {
        this.quickSession = quickSession;
    }

    public int[] portsAlreadySet() {
        final List<Integer> value = new ArrayList<Integer>();
        if (stopPort > 0) {
            value.add(stopPort);
        }
        if (httpPort > 0) {
            value.add(httpPort);
        }
        if (httpsPort > 0) {
            value.add(httpsPort);
        }
        return toInts(value);
    }

    protected int[] toInts(final List<Integer> values) {
        final int[] array = new int[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    public String getStopHost() {
        return stopHost;
    }

    public void setStopHost(final String stopHost) {
        this.stopHost = stopHost;
    }

    public String getStopCommand() {
        return stopCommand + Character.toString((char) 0); // last char to avoid warning/error log message
    }

    public void setStopCommand(final String stopCommand) {
        this.stopCommand = stopCommand;
    }

    public String getPreloadClasses() {
        return preloadClasses;
    }

    public void setPreloadClasses(final String preloadClasses) {
        this.preloadClasses = preloadClasses;
    }

    public String getWebContextToUseWithEars() {
        return webContextToUseWithEars;
    }

    public void setWebContextToUseWithEars(final String webContextToUseWithEars) {
        this.webContextToUseWithEars = webContextToUseWithEars;
    }

    public boolean getKeepServerXmlAsThis() {
        return keepServerXmlAsThis;
    }

    public void setKeepServerXmlAsThis(final boolean keepServerXmlAsThis) {
        this.keepServerXmlAsThis = keepServerXmlAsThis;
    }

    public boolean isSingleDumpByArchiveName() {
        return singleDumpByArchiveName;
    }

    public void setSingleDumpByArchiveName(final boolean singleDumpByArchiveName) {
        this.singleDumpByArchiveName = singleDumpByArchiveName;
    }

    public boolean isSingleDeploymentByArchiveName(final String name) {
        return singleDeploymentByArchiveName.contains(name) || singleDeploymentByArchiveName.contains("*") || singleDeploymentByArchiveName.contains("true");
    }

    public void setSingleDeploymentByArchiveName(final String singleDeploymentByArchiveName) {
        this.singleDeploymentByArchiveName = singleDeploymentByArchiveName == null || singleDeploymentByArchiveName.trim().isEmpty() ?
                Collections.<String>emptyList() : new HashSet<String>(asList(singleDeploymentByArchiveName.split(" *, *")));
        this.singleDumpByArchiveName = true; // implied otherwise what would be the sense?
    }
}
