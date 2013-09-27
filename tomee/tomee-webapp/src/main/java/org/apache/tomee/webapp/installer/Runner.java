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

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Runner {
    private final File openejbWarDir;
    private String catalinaHome = System.getProperty("catalina.home");
    private String catalinaBase = System.getProperty("catalina.base");
    private String serverXmlFile = System.getProperty("catalina.base") + "/conf/server.xml";

    public Runner(File openejbWarDir) {
        this.openejbWarDir = openejbWarDir;
    }

    public void setCatalinaHome(String catalinaHome) {
        this.catalinaHome = catalinaHome;
    }

    public void setCatalinaBaseDir(String catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    public void setServerXmlFile(String serverXmlFile) {
        this.serverXmlFile = serverXmlFile;
    }

    public Map<String, Object> execute() {
        final Paths paths = new Paths(openejbWarDir);
        final Installer installer = new Installer(paths);
        final Map<String, Object> result = new HashMap<String, Object>();
        if (Installer.Status.NONE.equals(installer.getStatus())) {
            paths.reset();
            installer.reset();
            paths.setCatalinaHomeDir(this.catalinaHome);
            paths.setCatalinaBaseDir(this.catalinaBase);
            paths.setServerXmlFile(this.serverXmlFile);
            if (paths.verify()) {
                installer.installAll();
            }
        }
        result.put("status", installer.getStatus());
        result.put("errors", installer.getAlerts().getErrors());
        result.put("warnings", installer.getAlerts().getWarnings());
        result.put("infos", installer.getAlerts().getInfos());
        final Map<String, Object> test = new HashMap<String, Object>();
        result.put("tests", test);
        {
            test.put("hasHome", false);
            test.put("doesHomeExist", false);
            test.put("isHomeDirectory", false);
            test.put("hasLibDirectory", false);
            final String homePath = System.getProperty("openejb.home");
            if (homePath != null) {
                test.put("hasHome", true);
                final File homeDir = new File(homePath);
                test.put("doesHomeExist", homeDir.exists());
                if (homeDir.exists()) {
                    test.put("isHomeDirectory", homeDir.isDirectory());
                    final File libDir = new File(homeDir, "lib");
                    test.put("hasLibDirectory", libDir.exists());
                }
            }
        }
        {
            test.put("wereTheOpenEJBClassesInstalled", false);
            test.put("wereTheEjbClassesInstalled", false);
            test.put("wasOpenEJBStarted", false);
            test.put("canILookupAnything", false);
            try {
                final ClassLoader myLoader = this.getClass().getClassLoader();
                Class.forName("org.apache.openejb.OpenEJB", true, myLoader);
                test.put("wereTheOpenEJBClassesInstalled", true);
            } catch (Exception e) {
                // noop
            }
            try {
                Class.forName("javax.ejb.EJBHome", true, this.getClass().getClassLoader());
                test.put("wereTheEjbClassesInstalled", true);
            } catch (Exception e) {
                // noop
            }
            try {
                final Class openejb = Class.forName("org.apache.openejb.OpenEJB", true, this.getClass().getClassLoader());
                final Method isInitialized = openejb.getDeclaredMethod("isInitialized");
                final Boolean running = (Boolean) isInitialized.invoke(openejb);
                test.put("wasOpenEJBStarted", running);
            } catch (Exception e) {
                // noop
            }
            try {
                final Properties p = new Properties();
                p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
                p.put("openejb.loader", "embed");
                final InitialContext ctx = new InitialContext(p);
                final Object obj = ctx.lookup("");
                if (obj.getClass().getName().equals("org.apache.openejb.core.ivm.naming.IvmContext")) {
                    test.put("canILookupAnything", true);
                }
            } catch (Exception e) {
                // noop
            }
        }
        return result;
    }
}
