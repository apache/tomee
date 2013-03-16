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
package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.Prefixes;
import org.apache.openejb.arquillian.common.TomEEConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@Prefixes({"tomee", "tomee.remote"})
public class RemoteTomEEConfiguration extends TomEEConfiguration {

    private String groupId = "org.apache.openejb";
    private String artifactId = "apache-tomee";
    private String version = "LATEST";
    private String classifier = "webprofile";
    private String type = "zip";
    private boolean removeUnusedWebapps = true;
    private int ajpPort = 8009;
    private String conf;
    private String bin;
    private String lib;
    private boolean cleanOnStartUp;
    private boolean debug;
    private int debugPort = 5005;
    private String catalina_opts = null; // using this format to match the script one
    private boolean simpleLog = false;

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

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArtifactName() {

        final String format = classifier == null ? "%s:%s:%s:%s" : "%s:%s:%s:%s:%s";

        return String.format(format, getGroupId(), getArtifactId(), getVersion(), getType(), getClassifier());

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

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
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

    public void setCleanOnStartUp(boolean clearOnStartUp) {
        this.cleanOnStartUp = clearOnStartUp;
    }

    public boolean getCleanOnStartUp() {
        return cleanOnStartUp;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public String getCatalina_opts() {
        return catalina_opts;
    }

    public void setCatalina_opts(final String opts) {
        this.catalina_opts = opts;
    }

    public boolean isSimpleLog() {
        return simpleLog;
    }

    public void setSimpleLog(final boolean simpleLog) {
        this.simpleLog = simpleLog;
    }
}
