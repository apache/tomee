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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Synchronization extends AbstractSynchronizable {
    private File resourcesDir;
    private File binariesDir;
    private File targetBinariesDir;
    private File targetResourcesDir;

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

    @Override
    public Map<File, File> updates() {
        if (updates == null) {
            updates = new HashMap<File, File>();
            if (resourcesDir != null && targetResourcesDir != null) {
                updates.put(resourcesDir, targetResourcesDir);
            }
            if (binariesDir != null && targetBinariesDir != null) {
                updates.put(binariesDir, targetBinariesDir);
            }
        }
        return updates;
    }
}
