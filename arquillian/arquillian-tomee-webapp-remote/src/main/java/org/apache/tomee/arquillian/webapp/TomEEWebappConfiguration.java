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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.arquillian.webapp;

import org.apache.openejb.arquillian.common.Prefixes;
import org.apache.openejb.arquillian.common.TomEEConfiguration;
import org.apache.openejb.resolver.maven.VersionResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@Prefixes({"tomee", "tomee.webapp"})
public class TomEEWebappConfiguration extends TomEEConfiguration {
    private static final String TOMCAT_REFERENCE_ARTIFACT = "org.apache.tomcat:tomcat-catalina";
    private static final String TOMCAT_VERSION_PREFIX = "7.";
    private static final String DEFAULT_TOMCAT_VERSION = TOMCAT_VERSION_PREFIX + "0.29";

    private String tomcatVersion = null;
    private String groupId = "org.apache.openejb";
    private String artifactId = "tomee-webapp";
    private String version = "LATEST";
    private String type = "war";
    private boolean removeUnusedWebapps = true;
    private boolean useInstallerServlet = false;
    private int ajpPort = 8009;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUseInstallerServlet() {
        return useInstallerServlet;
    }

    public void setUseInstallerServlet(boolean useInstallerServlet) {
        this.useInstallerServlet = useInstallerServlet;
    }

    public String getArtifactName() {

        return String.format("%s:%s:%s:%s", getGroupId(), getArtifactId(), getVersion(), getType());

    }

    public String getTomcatVersion() {
        if (tomcatVersion == null || "LATEST".equals(tomcatVersion)) {
            tomcatVersion = VersionResolver.highestVersion(TOMCAT_REFERENCE_ARTIFACT, TOMCAT_VERSION_PREFIX, DEFAULT_TOMCAT_VERSION);
        }
        return tomcatVersion;
    }

    public void setTomcatVersion(String tomcatVersion) {
        this.tomcatVersion = tomcatVersion;
    }

    public boolean isRemoveUnusedWebapps() {
        return removeUnusedWebapps;
    }

    public void setRemoveUnusedWebapps(boolean removeUnusedWebapps) {
        this.removeUnusedWebapps = removeUnusedWebapps;
    }

    public int getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(int ajpPort) {
        this.ajpPort = ajpPort;
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
        if (getAjpPort() > 0) {
            value.add(getAjpPort());
        }
        return toInts(value);
    }
}
