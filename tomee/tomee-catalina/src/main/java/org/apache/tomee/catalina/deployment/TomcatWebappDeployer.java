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
package org.apache.tomee.catalina.deployment;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public class TomcatWebappDeployer implements WebAppDeployer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomcatWebappDeployer.class);

    @Override
    public AppInfo deploy(final String context, final File file) {
        final TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);

        final Collection<String> alreadyDeployed = tomcatWebAppBuilder.availableApps();

        try {
            tomcatWebAppBuilder.deployWebApps(fakeInfo(file, context), null); // classloader == null -> standalone war
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }

        TomcatWebAppBuilder.ContextInfo info = contextInfo(file);

        if (info == null) { // try another time doing a diff with apps before deployment and apps after
            final Collection<String> deployedNow = tomcatWebAppBuilder.availableApps();
            final Iterator<String> it = deployedNow.iterator();
            while (it.hasNext()) {
                if (alreadyDeployed.contains(it.next())) {
                    it.remove();
                }
            }

            if (deployedNow.size() == 1) {
                info = contextInfo(new File(deployedNow.iterator().next()));
            }
        }

        if (info == null || info.appInfo == null) {
            LOGGER.error("Can't find of appInfo for " + (file != null ? file.getAbsolutePath() : null) + ", availables: " + tomcatWebAppBuilder.availableApps());
        }

        if (info == null) { // error
            return null;
        }
        return info.appInfo;
    }

    @Override
    public void reload(final String path) {
        final File file = new File(path);
        final TomcatWebAppBuilder.ContextInfo info = contextInfo(file);
        if (info == null || info.standardContext == null) { // error
            LOGGER.warning("Can't find " + path);
        } else {
            if (info.standardContext.getReloadable()) {
                info.standardContext.reload();
            }
        }
    }

    private TomcatWebAppBuilder.ContextInfo contextInfo(final File file) {
        final TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        return tomcatWebAppBuilder.standaAloneWebAppInfo(file);
    }

    // simply create a fake AppInfo to be able to deploy reusing the logic we already have
    private static AppInfo fakeInfo(final File file, final String context) {
        final AppInfo info = new AppInfo();
        info.path = file.getAbsolutePath();
        info.webAppAlone = true;

        final WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.path = info.path;

        if (context == null) {
            webAppInfo.contextRoot = file.getName();
        } else {
            webAppInfo.contextRoot = context;
        }

        webAppInfo.moduleId = webAppInfo.contextRoot;
        info.webApps.add(webAppInfo);

        return info;
    }
}
