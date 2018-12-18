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
import java.util.HashMap;
import java.util.Map;

/**
 * The type Synch for setting up the sources.
 */
public class Synch extends AbstractSynchronizable {

    private File source;
    private File target;

    /**
     * Gets source.
     *
     * @return the source
     */
    public File getSource() {
        return source;
    }

    /**
     * Sets source.
     *
     * @param source the source
     */
    public void setSource(final File source) {
        this.source = source;
    }

    /**
     * Gets target.
     *
     * @return the target
     */
    public File getTarget() {
        return target;
    }

    /**
     * Sets target.
     *
     * @param target the target
     */
    public void setTarget(final File target) {
        this.target = target;
    }

    @Override
    public Map<File, File> updates() {
        if (updates == null) {
            updates = new HashMap<File, File>();
            if (source != null && target != null) {
                updates.put(source, target);
            }
        }
        return updates;
    }
}
