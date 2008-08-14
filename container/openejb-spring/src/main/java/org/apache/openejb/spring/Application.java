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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.springframework.beans.factory.annotation.Required;

@Exported
public class Application {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, Application.class);
    private final ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private final List<AppInfo> applications = new ArrayList<AppInfo>();
    private final Map<Object, DeploymentInfo> notDeployed = new LinkedHashMap<Object, DeploymentInfo>();

    private OpenEJB openEJB;
    private boolean classpathAsEar = true;
    private boolean started = false;

    public OpenEJB getOpenEJB() {
        return openEJB;
    }

    public void setOpenEJB(OpenEJB openEJB) {
        this.openEJB = openEJB;
    }

    public boolean isClasspathAsEar() {
        return classpathAsEar;
    }

    public void setClasspathAsEar(boolean classpathAsEar) {
        this.classpathAsEar = classpathAsEar;
    }

    // Do not make this the Spring start method
    public void deployApplication() throws OpenEJBException {
        // Ok someone made this the OpenEJB start method... ignore this deploy call
        if (openEJB != null && openEJB.isStarting()) return;

        if (started) return;
        started = true;

        Set<String> declaredApplications = getDeployedApplications();

        List<String> classpathApps = new ArrayList<String>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        DeploymentsResolver.loadFromClasspath(SystemInstance.get().getBase(), classpathApps, classLoader);

        ArrayList<File> jarFiles = new ArrayList<File>();
        for (String path : classpathApps) {
            if (declaredApplications.contains(path)) continue;
            // todo hack to avoid picking up application.xml in openejb-core module
            if (path.indexOf("openejb-core/target/test-classes") > 0) continue;

            jarFiles.add(new File(path));
        }

        Assembler assembler = getAssembler();
        if (classpathAsEar) {
            AppInfo appInfo = configurationFactory.configureApplication(classLoader, "classpath.ear", jarFiles);
            deployApplication(assembler, appInfo);
        } else {
            for (File jarFile : jarFiles) {
                AppInfo appInfo = configurationFactory.configureApplication(jarFile);
                deployApplication(assembler, appInfo);
            }
        }
    }

    private void deployApplication(Assembler assembler, AppInfo appInfo) throws OpenEJBException {
        try {
            List<DeploymentInfo> deployments = assembler.createApplication(appInfo, assembler.createAppClassLoader(appInfo), false);
            for (DeploymentInfo deployment : deployments) {
                this.notDeployed.put(deployment.getDeploymentID(), deployment);
            }
            applications.add(appInfo);
        } catch (Exception e) {
            if (e instanceof OpenEJBException) {
                throw (OpenEJBException) e;
            }
            throw new OpenEJBException("Error starting application " + appInfo.jarPath, e);
        }
    }

    @PreDestroy
    public void stop() {
        if (!started) return;
        started = false;

        for (AppInfo application : applications) {
            try {
                getAssembler().destroyApplication(application.jarPath);
            } catch (Exception e) {
                logger.error("Error stopping application " + application.jarPath, e);
            }
        }
    }

    public void startEjb(Object deploymentId) throws OpenEJBException {
        if (deploymentId == null) throw new NullPointerException("deploymentId is null");

        deployApplication();

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo deployment = containerSystem.getDeploymentInfo(deploymentId);
        if (deployment == null) {
            throw new IllegalArgumentException("Unknwon EJB " + deployment);
        }

        deployment = notDeployed.remove(deploymentId);
        if (deployment == null) {
            // already deployed
            return;
        }

        Container container = deployment.getContainer();
        container.deploy(deployment);
        logger.info("createApplication.createdEjb", deployment.getDeploymentID(), deployment.getEjbName(), container.getContainerID());
    }

    private Assembler getAssembler() {
        if (openEJB != null) {
            return openEJB.getAssembler();
        } else {
            return SystemInstance.get().getComponent(Assembler.class);
        }
    }

    private Set<String> getDeployedApplications() {
        Set<String> declaredApps = new TreeSet<String>();
        Collection<AppInfo> applications = getAssembler().getDeployedApplications();
        for (AppInfo application : applications) {
            declaredApps.add(application.jarPath);
            for (EjbJarInfo ejbJar : application.ejbJars) {
                declaredApps.add(ejbJar.jarPath);
            }
            for (ConnectorInfo connector : application.connectors) {
                declaredApps.add(connector.codebase);
            }
            for (WebAppInfo webApp : application.webApps) {
                declaredApps.add(webApp.codebase);
            }
            for (ClientInfo client : application.clients) {
                declaredApps.add(client.codebase);
            }
        }
        return declaredApps;
    }
}
