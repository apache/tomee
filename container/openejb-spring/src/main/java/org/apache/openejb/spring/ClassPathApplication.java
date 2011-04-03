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
package org.apache.openejb.spring;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;

@Exported
public class ClassPathApplication extends AbstractApplication {
    private final ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private boolean classpathAsEar = true;

    public boolean isClasspathAsEar() {
        return classpathAsEar;
    }

    public void setClasspathAsEar(boolean classpathAsEar) {
        this.classpathAsEar = classpathAsEar;
    }

    protected List<AppInfo> loadApplications() throws OpenEJBException {
        Set<String> declaredApplications = getDeployedApplications();

        List<URL> classpathApps = new ArrayList<URL>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        DeploymentsResolver.loadFromClasspath(SystemInstance.get().getBase(), classpathApps, classLoader);

        ArrayList<File> jarFiles = new ArrayList<File>();
        for (URL path : classpathApps) {
            if (declaredApplications.contains(URLs.toFilePath(path))) continue;
            // todo hack to avoid picking up application.xml in openejb-core module
            if (URLs.toFilePath(path).indexOf("openejb-core/target/test-classes") > 0) continue;

            jarFiles.add(URLs.toFile(path));
        }

        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        if (classpathAsEar) {
            AppInfo appInfo = configurationFactory.configureApplication(classLoader, "classpath.ear", jarFiles);
            appInfos.add(appInfo);
        } else {
            for (File jarFile : jarFiles) {
                AppInfo appInfo = configurationFactory.configureApplication(jarFile);
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    private Set<String> getDeployedApplications() {
        Set<String> declaredApps = new TreeSet<String>();
        Collection<AppInfo> applications = getAssembler().getDeployedApplications();
        for (AppInfo application : applications) {
            declaredApps.add(application.path);
            for (EjbJarInfo ejbJar : application.ejbJars) {
                declaredApps.add(ejbJar.path);
            }
            for (ConnectorInfo connector : application.connectors) {
                declaredApps.add(connector.path);
            }
            for (WebAppInfo webApp : application.webApps) {
                declaredApps.add(webApp.path);
            }
            for (ClientInfo client : application.clients) {
                declaredApps.add(client.path);
            }
        }
        return declaredApps;
    }
}
