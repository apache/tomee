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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.JndiBuilder;
import org.apache.openejb.assembler.classic.JndiBuilder.JndiNameStrategy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractApplication implements ApplicationContextAware {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ClassPathApplication.class);
    private final Map<String, JndiNameStrategy> nameStrategies = new TreeMap<String, JndiNameStrategy>();

    protected final List<AppInfo> applications = new ArrayList<AppInfo>();
    protected OpenEJB openEJB;
    protected boolean started = false;
    protected boolean export = true;
    private ConfigurableApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    public OpenEJB getOpenEJB() {
        return openEJB;
    }

    public void setOpenEJB(OpenEJB openEJB) {
        this.openEJB = openEJB;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    protected abstract List<AppInfo> loadApplications() throws OpenEJBException;

    @PostConstruct
    public void start() throws OpenEJBException {
        // check if openejb already started
        if (openEJB == null) {
            Map map = applicationContext.getBeansOfType(OpenEJB.class);
            if (!map.isEmpty()) {
                openEJB = (OpenEJB) map.values().iterator().next();
            }
        }
        if (openEJB == null) {
            openEJB = new OpenEJB();
            applicationContext.getBeanFactory().registerSingleton("openejb.internalOpenEJB", openEJB);
            openEJB.setApplicationContext(applicationContext);
            openEJB.start();
        }
        openEJB.deployApplication(this);
    }

    // Do not make this the Spring start method
    protected void deployApplication() throws OpenEJBException {
        // Ok someone made this the OpenEJB start method... ignore this deploy call
        if (openEJB != null && openEJB.isStarting()) return;

        if (started) return;
        started = true;

        // load the applications
        List<AppInfo> appInfos = loadApplications();

        // deploy the applications
        Assembler assembler = getAssembler();
        for (AppInfo appInfo : appInfos) {
            try {
                List<DeploymentInfo> deployments = assembler.createApplication(appInfo, assembler.createAppClassLoader(appInfo), true);
                if (export) {
                    for (DeploymentInfo deployment : deployments) {
                        JndiNameStrategy strategy = createStrategy(appInfo, deployments, deployment);
                        Map<String, EJB> bindings = getEjbBindings(strategy, (CoreDeploymentInfo) deployment);
                        for (Entry<String, EJB> entry : bindings.entrySet()) {
                            String beanName = entry.getKey();
                            if (!applicationContext.containsBean(beanName)) {
                                EJB ejb = entry.getValue();
                                applicationContext.getBeanFactory().registerSingleton(beanName, ejb);
                                logger.info("Exported EJB " + deployment.getEjbName() + " with interface " + entry.getValue().getInterface().getName() + " to Spring bean " + entry.getKey());
                            }
                        }
                    }
                }
                applications.add(appInfo);
            } catch (Exception e) {
                if (e instanceof OpenEJBException) {
                    throw (OpenEJBException) e;
                }
                throw new OpenEJBException("Error starting application " + appInfo.jarPath, e);
            }
        }
    }

    public Map<String, EJB> getEjbBindings(JndiNameStrategy strategy, CoreDeploymentInfo deployment) {
        strategy.begin(deployment);

        Map<String, EJB> bindings = new TreeMap<String, EJB>();

        Class remoteHome = deployment.getHomeInterface();
        if (remoteHome != null) {
            String externalName = strategy.getName(remoteHome, JndiNameStrategy.Interface.REMOTE_HOME);
            bindings.put(externalName, new EJB(deployment, remoteHome));
        }


        Class localHome = deployment.getLocalHomeInterface();
        if (localHome != null) {
            String externalName = strategy.getName(localHome, JndiNameStrategy.Interface.LOCAL_HOME);
            bindings.put(externalName, new EJB(deployment, remoteHome));
        }

        for (Class businessLocal : deployment.getBusinessLocalInterfaces()) {
            String externalName = strategy.getName(businessLocal, JndiNameStrategy.Interface.BUSINESS_LOCAL);
            bindings.put(externalName, new EJB(deployment, businessLocal));
        }

        for (Class businessRemote : deployment.getBusinessRemoteInterfaces()) {
            String externalName = strategy.getName(businessRemote, JndiNameStrategy.Interface.BUSINESS_REMOTE);
            bindings.put(externalName, new EJB(deployment, businessRemote));
        }

//        if (MessageListener.class.equals(deployment.getMdbInterface())) {
//            String name = deployment.getDeploymentID().toString();
//            bindings.put(name, MessageListener.class);
//        }

        strategy.end();

        return bindings;
    }

    public JndiNameStrategy createStrategy(AppInfo appInfo, List<DeploymentInfo> deployments, DeploymentInfo deployment) throws OpenEJBException {
        JndiNameStrategy strategy = nameStrategies.get(deployment.getModuleID());
        if (strategy != null) {
            return strategy;
        }

        String deploymentId = (String) deployment.getDeploymentID();
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            if (ejbJar.moduleId.equals(deployment.getModuleID())) {
                Set<String> moduleDeploymentIds = new TreeSet<String>();
                for (EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
                    moduleDeploymentIds.add(enterpriseBean.ejbDeploymentId);
                }
                Map<String, DeploymentInfo> moduleDeployments = new TreeMap<String, DeploymentInfo>();
                for (DeploymentInfo deploymentInfo : deployments) {
                    if (moduleDeploymentIds.contains(deploymentId)) {
                        moduleDeployments.put((String) deploymentInfo.getDeploymentID(), deploymentInfo);
                    }
                }
                strategy = JndiBuilder.createStrategy(ejbJar, moduleDeployments);
                for (String moduleDeploymentId : moduleDeploymentIds) {
                    nameStrategies.put(moduleDeploymentId, strategy);
                }
                return strategy;
            }
        }
        throw new OpenEJBException("Can not find EjbJarInfo " + deployment.getModuleID() + " for EJB " + deploymentId);
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

    protected Assembler getAssembler() {
            return SystemInstance.get().getComponent(Assembler.class);
    }
}
