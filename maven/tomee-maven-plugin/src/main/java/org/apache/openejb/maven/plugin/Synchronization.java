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

/**
 * The type Synchronization to sunc the files and directories.
 */
public class Synchronization extends AbstractSynchronizable {
    private File resourcesDir;
    private File binariesDir;
    private File targetBinariesDir;
    private File targetResourcesDir;

    /**
     * Gets resources dir.
     *
     * @return the resources dir
     */
    public File getResourcesDir() {
        return resourcesDir;
    }

    /**
     * Sets resources dir.
     *
     * @param resourcesDir the resources dir
     */
    public void setResourcesDir(final File resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    /**
     * Gets binaries dir.
     *
     * @return the binaries dir
     */
    public File getBinariesDir() {
        return binariesDir;
    }

    /**
     * Sets binaries dir.
     *
     * @param binariesDir the binaries dir
     */
    public void setBinariesDir(final File binariesDir) {
        this.binariesDir = binariesDir;
    }

    /**
     * Gets target binaries dir.
     *
     * @return the target binaries dir
     */
    public File getTargetBinariesDir() {
        return targetBinariesDir;
    }

    /**
     * Sets target binaries dir.
     *
     * @param targetBinariesDir the target binaries dir
     */
    public void setTargetBinariesDir(final File targetBinariesDir) {
        this.targetBinariesDir = targetBinariesDir;
    }

    /**
     * Gets target resources dir.
     *
     * @return the target resources dir
     */
    public File getTargetResourcesDir() {
        return targetResourcesDir;
    }

    /**
     * Sets target resources dir.
     *
     * @param targetResourcesDir the target resources dir
     */
    public void setTargetResourcesDir(final File targetResourcesDir) {
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
