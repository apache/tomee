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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomee.webapp.command.impl;

import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.webapp.Application;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.IsProtected;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@IsProtected
public class GetStatus implements Command {

    @Override
    public Object execute(final Map<String, Object> params) throws Exception {
        final String path = Application.getInstance().getRootPath();
        File openejbWarDir = null;
        if (path != null) {
            openejbWarDir = new File(path);
        }
        final Paths paths = new Paths(openejbWarDir);

        final Map<String, Object> json = new HashMap<String, Object>();
        json.put("isListenerInstalled", Installer.isListenerInstalled());
        json.put("isAgentInstalled", Installer.isAgentInstalled());


        final Map<String, String> strPaths = new HashMap<String, String>();
        strPaths.put("catalina-conf", getSafePath(paths.getCatalinaConfDir()));
        strPaths.put("catalina-lib", getSafePath(paths.getCatalinaLibDir()));
        strPaths.put("catalina-bin", getSafePath(paths.getCatalinaBinDir()));
        strPaths.put("catalina-catalina-sh", getSafePath(paths.getCatalinaShFile()));
        strPaths.put("catalina-catalina-bat", getSafePath(paths.getCatalinaBatFile()));
        strPaths.put("openEJB-lib", getSafePath(paths.getOpenEJBLibDir()));
        strPaths.put("openEJB-loader-jar", getSafePath(paths.getOpenEJBTomcatLoaderJar()));
        strPaths.put("openEJB-javaagent-jar", getSafePath(paths.getOpenEJBJavaagentJar()));


        final Map<String, String> strMainPaths = new HashMap<String, String>();
        strMainPaths.put("catalina-home", getSafePath(paths.getCatalinaHomeDir()));
        strMainPaths.put("catalina-base", getSafePath(paths.getCatalinaBaseDir()));
        strMainPaths.put("catalina-server-xml", getSafePath(paths.getServerXmlFile()));

        json.put("paths", strPaths);
        json.put("mainPaths", strMainPaths);

        final Installer installer = new Installer(paths);
        json.put("installerStatus", installer.getStatus());
        return json;
    }

    private String getSafePath(File file) {
        if (file == null) {
            return "";
        }
        return file.getPath();
    }
}
