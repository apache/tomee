/**
 *
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
package org.apache.openejb.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Abstract synchronizable.
 */
public abstract class AbstractSynchronizable {
    /**
     * The Update interval.
     */
    protected int updateInterval;
    /**
     * The Extensions.
     */
    protected List<String> extensions;
    /**
     * The Update only extensions.
     */
    protected List<String> updateOnlyExtensions;
    /**
     * The Regex.
     */
    protected String regex;
    /**
     * The Updates.
     */
    protected Map<File, File> updates;

    /**
     * Updates map.
     *
     * @return the map
     */
    public abstract Map<File, File> updates();

    /**
     * Gets update interval.
     *
     * @return the update interval
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Sets update interval.
     *
     * @param updateInterval the update interval
     */
    public void setUpdateInterval(final int updateInterval) {
        this.updateInterval = updateInterval;
    }

    /**
     * Gets extensions.
     *
     * @return the extensions
     */
    public List<String> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<>();
        }
        return extensions;
    }

    /**
     * Sets extensions.
     *
     * @param extensions the extensions
     */
    public void setExtensions(final List<String> extensions) {
        this.extensions = extensions;
    }

    /**
     * Gets regex.
     *
     * @return the regex
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Sets regex.
     *
     * @param regex the regex
     */
    public void setRegex(final String regex) {
        this.regex = regex;
    }

    /**
     * Gets update only extenions.
     *
     * @return the update only extenions
     */
    public List<String> getUpdateOnlyExtenions() {
        if (updateOnlyExtensions == null) {
            updateOnlyExtensions = new ArrayList<>();
        }
        return updateOnlyExtensions;
    }

    /**
     * Sets update only extensions.
     *
     * @param updateOnlyExtensions the update only extensions
     */
    public void setUpdateOnlyExtensions(final List<String> updateOnlyExtensions) {
        this.updateOnlyExtensions = updateOnlyExtensions;
    }
}
