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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.common;


import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.util.ArrayList;
import java.util.List;

public class TomEEConfiguration implements ContainerConfiguration {

    protected boolean exportConfAsSystemProperty = false;
    protected int httpPort = 8080;
    protected int stopPort = 8005;
    protected String dir = System.getProperty("java.io.tmpdir") + "/arquillian-apache-tomee";
    protected String appWorkingDir = System.getProperty("java.io.tmpdir");
    protected String host = "localhost";
    protected String stopHost = "localhost"; // generally localhost but host (http) can be different
    protected String stopCommand = "SHUTDOWN"; // default one - can be overriden in server.xml
    protected String serverXml = null;
    protected String properties = "";
    protected String portRange = ""; // only used if port < 0, empty means whatever, can be "1024-65535"
    protected boolean quickSession = true;
    protected boolean unpackWars = true;

    public boolean isUnpackWars() {
        return unpackWars;
    }

    public void setUnpackWars(boolean unpackWars) {
        this.unpackWars = unpackWars;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getStopPort() {
        return stopPort;
    }

    public void setStopPort(int stopPort) {
        this.stopPort = stopPort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getAppWorkingDir() {
        return appWorkingDir;
    }

    public void setAppWorkingDir(String appWorkingDir) {
        this.appWorkingDir = appWorkingDir;
    }

    public void validate() throws ConfigurationException {
    }

    public boolean getExportConfAsSystemProperty() {
        return exportConfAsSystemProperty;
    }

    public void setExportConfAsSystemProperty(boolean exportConfAsSystemProperty) {
        this.exportConfAsSystemProperty = exportConfAsSystemProperty;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServerXml() {
        return serverXml;
    }

    public void setServerXml(String serverXml) {
        this.serverXml = serverXml;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String systemProperties() {
        return properties.replaceAll("\n *", "\n");
    }

    public String getPortRange() {
        return portRange;
    }

    public void setPortRange(String portRange) {
        this.portRange = portRange;
    }

    public boolean isQuickSession() {
        return quickSession;
    }

    public void setQuickSession(boolean quickSession) {
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
        return toInts(value);
    }

    protected int[] toInts(List<Integer> values) {
        int[] array = new int[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    public String getStopHost() {
        return stopHost;
    }

    public void setStopHost(String stopHost) {
        this.stopHost = stopHost;
    }

    public String getStopCommand() {
        return stopCommand +  Character.toString((char) 0); // last char to avoid warning/error log message
    }

    public void setStopCommand(String stopCommand) {
        this.stopCommand = stopCommand;
    }
}
