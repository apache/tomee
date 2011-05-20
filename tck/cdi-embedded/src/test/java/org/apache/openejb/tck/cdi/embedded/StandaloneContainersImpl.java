/**
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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.jboss.testharness.api.DeploymentException;

import javax.enterprise.inject.spi.BeanManager;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneContainersImpl implements org.jboss.testharness.spi.StandaloneContainers {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tck"), StandaloneContainersImpl.class);

    private AppContext appContext;

    private DeploymentException deploymentException;

    public void deploy(Collection<Class<?>> classes) throws DeploymentException {
        deploy(classes, Collections.<URL>emptyList());
    }

    public boolean deploy(Collection<Class<?>> classes, Collection<URL> urls) {
        logger.debug("StandaloneContainersImpl.deploy");

        List<String> classNames = new ArrayList<String>();

        for (Class<?> clazz : classes) classNames.add(clazz.getName());

        if (logger.isDebugEnabled()) {
            Collections.sort(classNames);

            for (String clazz : classNames) {
                logger.debug("clazz = " + clazz);
            }

            for (URL url : urls) {
                logger.debug("url = " + url);
            }
        }

        try {
            EjbModule ejbModule = new EjbModule(new EjbJar("beans"));
            ejbModule.setFinder(new AnnotationFinder(new ClassesArchive(classes)));

            Map<String, Object> dds = ejbModule.getAltDDs();

            for (URL url : urls) {
                final File file = new File(url.getFile());
                dds.put(file.getName(), url);
            }

            Assembler assembler = new Assembler();
            ConfigurationFactory config = new ConfigurationFactory();
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));

            final AppInfo appInfo = config.configureApplication(new AppModule(ejbModule));

            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                // TODO We need to improve our scanning so this isn't necessary
                ejbJar.beans.managedClasses.addAll(classNames);
            }

            appContext = assembler.createApplication(appInfo);

            // This must be set or the OWB static lookup code won't work and everything will fall apart
            Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
        } catch (Exception e) {
            logger.debug("Deploy failed", e);
            deploymentException = new DeploymentException("Deploy failed", e);
            return false;
        }
        //            logger.debug("StandaloneContainersImpl.deploy(classes, urls)");
        //            for (Class<?> clazz : classes) {
        //                logger.debug("clazz = " + clazz);
        //            }
        //            for (URL url : urls) {
        //                logger.debug("url = " + url);
        //            }


        ThreadSingletonServiceImpl.enter(appContext.getWebBeansContext());

        return true;
    }

    public DeploymentException getDeploymentException() {
        logger.debug("StandaloneContainersImpl.getDeploymentException");
        return deploymentException;
    }

    public void undeploy() {
        logger.debug("StandaloneContainersImpl.undeploy");
    }

    public void setup() {
        logger.debug("StandaloneContainersImpl.setup");
    }

    public void cleanup() {
        logger.debug("StandaloneContainersImpl.cleanup");
    }
}
