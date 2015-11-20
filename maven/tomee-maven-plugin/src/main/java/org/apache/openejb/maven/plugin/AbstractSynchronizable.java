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

public abstract class AbstractSynchronizable {
    protected int updateInterval;
    protected List<String> extensions;
    protected List<String> updateOnlyExtensions;
    protected String regex;
    protected Map<File, File> updates;

    public abstract Map<File, File> updates();

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(final int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public List<String> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<String>();
        }
        return extensions;
    }

    public void setExtensions(final List<String> extensions) {
        this.extensions = extensions;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(final String regex) {
        this.regex = regex;
    }

    public List<String> getUpdateOnlyExtenions() {
        if (updateOnlyExtensions == null) {
            updateOnlyExtensions = new ArrayList<String>();
        }
        return updateOnlyExtensions;
    }

    public void setUpdateOnlyExtensions(final List<String> updateOnlyExtensions) {
        this.updateOnlyExtensions = updateOnlyExtensions;
    }
}
