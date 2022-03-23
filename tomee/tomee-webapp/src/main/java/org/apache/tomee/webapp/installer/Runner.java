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

import org.apache.tomee.installer.InstallerInterface;
import org.apache.tomee.installer.PathsInterface;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Runner {
    private final InstallerInterface installer;
    private String catalinaHome = System.getProperty("catalina.home");
    private String catalinaBase = System.getProperty("catalina.base");
    private String serverXmlFile = System.getProperty("catalina.base") + "/conf/server.xml";

    private static List<Map<String, String>> installerResults;
    private static org.apache.tomee.installer.Status installerStatus;

    public Runner(final InstallerInterface installer) {
        this.installer = installer;
    }

    public void setCatalinaHome(final String catalinaHome) {
        this.catalinaHome = catalinaHome;
    }

    public void setCatalinaBaseDir(final String catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    public void setServerXmlFile(final String serverXmlFile) {
        this.serverXmlFile = serverXmlFile;
    }

    private void setAlerts(final String key, final List<String> messages) {
        if (messages == null) {
            return;
        }
        for (final String message : messages) {
            installerResults.add(Common.build(key, message));
        }
    }

    public synchronized List<Map<String, String>> execute(final boolean install) {
        if (org.apache.tomee.installer.Status.INSTALLED.equals(installerStatus) ||
                org.apache.tomee.installer.Status.REBOOT_REQUIRED.equals(installerStatus)) {
            return installerResults;
        }
        final PathsInterface paths = installer.getPaths();
        paths.reset();
        installer.reset();
        paths.setCatalinaHomeDir(this.catalinaHome);
        paths.setCatalinaBaseDir(this.catalinaBase);
        paths.setServerXmlFile(this.serverXmlFile);
        if (paths.verify() && install) {
            installer.installAll();
        }
        installerResults = new ArrayList<>();
        installerResults.add(Common.build("catalinaHomeDir", String.valueOf(catalinaHome)));
        installerResults.add(Common.build("catalinaBaseDir", String.valueOf(catalinaBase)));
        installerResults.add(Common.build("serverXmlFile", String.valueOf(serverXmlFile)));
        installerStatus = installer.getStatus();
        installerResults.add(Common.build("status", String.valueOf(installerStatus)));
        setAlerts("errors", installer.getAlerts().getErrors());
        setAlerts("warnings", installer.getAlerts().getWarnings());
        setAlerts("infos", installer.getAlerts().getInfos());
        {
            boolean hasHome = false;
            boolean doesHomeExist = false;
            boolean isHomeDirectory = false;
            boolean hasLibDirectory = false;
            final String homePath = System.getProperty("openejb.home");
            if (homePath != null) {
                hasHome = true;
                final File homeDir = new File(homePath);
                doesHomeExist = homeDir.exists();
                if (homeDir.exists()) {
                    isHomeDirectory = homeDir.isDirectory();
                    final File libDir = new File(homeDir, "lib");
                    hasLibDirectory = libDir.exists();
                }
            }
            installerResults.add(Common.build("hasHome", String.valueOf(hasHome)));
            installerResults.add(Common.build("doesHomeExist", String.valueOf(doesHomeExist)));
            installerResults.add(Common.build("isHomeDirectory", String.valueOf(isHomeDirectory)));
            installerResults.add(Common.build("hasLibDirectory", String.valueOf(hasLibDirectory)));
        }
        {
            boolean wereTheOpenEJBClassesInstalled = false;
            boolean wereTheEjbClassesInstalled = false;
            boolean wasOpenEJBStarted = false;
            boolean canILookupAnything = false;
            try {
                final ClassLoader myLoader = this.getClass().getClassLoader();
                Class.forName("org.apache.openejb.OpenEJB", true, myLoader);
                wereTheOpenEJBClassesInstalled = true;
            } catch (final Exception e) {
                // noop
            }
            try {
                Class.forName("jakarta.ejb.EJBHome", true, this.getClass().getClassLoader());
                wereTheEjbClassesInstalled = true;
            } catch (final Exception e) {
                // noop
            }
            try {
                final Class openejb = Class.forName("org.apache.openejb.OpenEJB", true, this.getClass().getClassLoader());
                final Method isInitialized = openejb.getDeclaredMethod("isInitialized");
                wasOpenEJBStarted = (Boolean) isInitialized.invoke(openejb);
            } catch (final Exception e) {
                // noop
            }
            try {
                final Properties p = new Properties();
                p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
                p.put("openejb.loader", "embed");
                final InitialContext ctx = new InitialContext(p);
                final Object obj = ctx.lookup("");
                if (obj.getClass().getName().equals("org.apache.openejb.core.ivm.naming.IvmContext")) {
                    canILookupAnything = true;
                }
            } catch (final Exception e) {
                // noop
            }
            installerResults.add(Common.build("wereTheOpenEJBClassesInstalled", String.valueOf(wereTheOpenEJBClassesInstalled)));
            installerResults.add(Common.build("wereTheEjbClassesInstalled", String.valueOf(wereTheEjbClassesInstalled)));
            installerResults.add(Common.build("wasOpenEJBStarted", String.valueOf(wasOpenEJBStarted)));
            installerResults.add(Common.build("canILookupAnything", String.valueOf(canILookupAnything)));
        }
        return installerResults;
    }
}
