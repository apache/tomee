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
package org.apache.openejb.maven.plugin;

import java.io.File;
import java.util.List;

public class Synchronization {
    private File resourcesDir;
    private File binariesDir;
    private File targetBinariesDir;
    private File targetResourcesDir;
    private int updateInterval;
    private List<String> extensions;
    private List<String> updateOnlyExtensions;
    private String regex;

    public File getResourcesDir() {
        return resourcesDir;
    }

    public void setResourcesDir(File resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    public File getBinariesDir() {
        return binariesDir;
    }

    public void setBinariesDir(File binariesDir) {
        this.binariesDir = binariesDir;
    }

    public File getTargetBinariesDir() {
        return targetBinariesDir;
    }

    public void setTargetBinariesDir(File targetBinariesDir) {
        this.targetBinariesDir = targetBinariesDir;
    }

    public File getTargetResourcesDir() {
        return targetResourcesDir;
    }

    public void setTargetResourcesDir(File targetResourcesDir) {
        this.targetResourcesDir = targetResourcesDir;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public List<String> getUpdateOnlyExtenions() {
        return updateOnlyExtensions;
    }

    public void setUpdateOnlyExtensions(List<String> updateOnlyExtensions) {
        this.updateOnlyExtensions = updateOnlyExtensions;
    }
}
