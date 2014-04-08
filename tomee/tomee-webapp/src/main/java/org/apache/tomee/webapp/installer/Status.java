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

package org.apache.tomee.webapp.installer;

import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Status {

    private final File openejbWarDir;

    public Status(File openejbWarDir) {
        this.openejbWarDir = openejbWarDir;
    }

    private String getSafePath(File file) {
        if (file == null) {
            return "";
        }
        return file.getPath();
    }

    public List<Map<String, String>> get() {
        final Paths paths = new Paths(openejbWarDir);
        final List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        final Installer installer = new Installer(paths);
        result.add(Common.build("status", String.valueOf(installer.getStatus())));
        result.add(Common.build("isListenerInstalled", String.valueOf(Installer.isListenerInstalled())));
        result.add(Common.build("isAgentInstalled", String.valueOf(Installer.isAgentInstalled())));
        result.add(Common.build("catalinaConfDir", getSafePath(paths.getCatalinaConfDir())));
        result.add(Common.build("catalinaLibDir", getSafePath(paths.getCatalinaLibDir())));
        result.add(Common.build("catalinaBinDir", getSafePath(paths.getCatalinaBinDir())));
        result.add(Common.build("catalinaShFile", getSafePath(paths.getCatalinaShFile())));
        result.add(Common.build("catalinaBatFile", getSafePath(paths.getCatalinaBatFile())));
        result.add(Common.build("openEJBLibDir", getSafePath(paths.getOpenEJBLibDir())));
        result.add(Common.build("openEJBTomcatLoaderJar", getSafePath(paths.getOpenEJBTomcatLoaderJar())));
        result.add(Common.build("openEJBJavaagentJar", getSafePath(paths.getOpenEJBJavaagentJar())));
        result.add(Common.build("catalinaHomeDir", getSafePath(paths.getCatalinaHomeDir())));
        result.add(Common.build("catalinaBaseDir", getSafePath(paths.getCatalinaBaseDir())));
        result.add(Common.build("serverXmlFile", getSafePath(paths.getServerXmlFile())));
        return result;
    }
}
