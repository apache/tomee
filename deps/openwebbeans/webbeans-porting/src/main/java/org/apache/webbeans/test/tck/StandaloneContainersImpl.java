/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.tck;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.lifecycle.StandaloneLifeCycle;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.test.tck.mock.TCKMetaDataDiscoveryImpl;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;

public class StandaloneContainersImpl implements StandaloneContainers
{
    /**Logger instance*/
    protected  final Logger logger = WebBeansLoggerFacade.getLogger(StandaloneContainersImpl.class);

    protected StandaloneLifeCycle lifeCycle = null;

    protected DeploymentException excpetion;

    public void deployInternal(Iterable<Class<?>> classes) throws DeploymentException
    {
        // Scanner service
        final TCKMetaDataDiscoveryImpl discovery = (TCKMetaDataDiscoveryImpl) WebBeansContext.getInstance().getScannerService();

        // Lifecycle container
        this.lifeCycle = new StandaloneLifeCycle()
        {
            protected void afterInitApplication(Properties event)
            {
                this.scannerService = discovery;
            }
        };

        try
        {
            Iterator<Class<?>> it = classes.iterator();
            while (it.hasNext())
            {
                discovery.addBeanClass(it.next());
            }

            this.lifeCycle.startApplication(null);

        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Standalone Container Impl.", e);
            this.excpetion = new DeploymentException("Standalone Container Impl.", e);
            throw this.excpetion;
        }

    }

    public boolean deployInternal(Iterable<Class<?>> classes, Iterable<URL> beansXmls)
    {
        try
        {
            final TCKMetaDataDiscoveryImpl discovery = (TCKMetaDataDiscoveryImpl) WebBeansContext.getInstance().getScannerService();

            // Lifecycle container
            this.lifeCycle = new StandaloneLifeCycle()
            {
                protected void afterInitApplication(Properties event)
                {
                    this.scannerService = discovery;
                }
            };

            Iterator<Class<?>> it = classes.iterator();
            while (it.hasNext())
            {
                discovery.addBeanClass(it.next());
            }

            Iterator<URL> itUrl = beansXmls.iterator();
            while (itUrl.hasNext())
            {
                discovery.addBeanXml(itUrl.next());
            }

            this.lifeCycle.startApplication(null);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Standalone Container Impl.", e);
            this.excpetion = new DeploymentException("Standalone Container Impl.", e);

            return false;
        }

        return true;
    }

    public void setup()
    {

    }

    public void cleanup()
    {

    }

    public void undeploy()
    {
        try
        {
            this.lifeCycle.stopApplication(null);
            this.lifeCycle = null;
        }
        finally
        {
            ManagersImpl.cleanUp();
        }
    }

    public DeploymentException getDeploymentException()
    {
        return this.excpetion;
    }

    public void deploy(Collection<Class<?>> classes) throws DeploymentException
    {
        setUp(classes);
        deployInternal(classes);
    }

    public boolean deploy(Collection<Class<?>> classes, Collection<URL> xmls)
    {
        if (!setUp(classes))
        {
            return false;
        }

        return deployInternal(classes, xmls);
    }

    /**
     * @param classes
     * @return <code>true</code> if the setup succeed, <code>false</code> otherwise.
     */
    private boolean setUp(Collection<Class<?>> classes)
    {

        try
        {
            ConfigurationFactory config = new ConfigurationFactory();
            Assembler assembler = new Assembler();

            assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

            assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));
            assembler.createContainer(config.configureService(StatefulSessionContainerInfo.class));
            assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

            EjbJarInfo ejbJar = config.configureApplication(buildTestApp(classes));

            assembler.createApplication(ejbJar);

            System.setProperty("openejb.validation.output.level", "VERBOSE");
            Properties properties = new Properties(System.getProperties());
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
            new InitialContext(properties);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Standalone Container Impl.", e);
            return false;
        }

        return true;
    }

    private EjbModule buildTestApp(Collection<Class<?>> classes)
    {
        EjbJar ejbJar = new EjbJar();
        ejbJar.setId(this.getClass().getName());

        for (Class<?> clazz : classes)
        {
            if (isSingleton(clazz))
            {
                ejbJar.addEnterpriseBean(new SingletonBean(clazz));
            }
            if (isStateless(clazz))
            {
                ejbJar.addEnterpriseBean(new StatelessBean(clazz));
            }

            if (isStatefull(clazz))
            {
                ejbJar.addEnterpriseBean(new StatefulBean(clazz));
            }
        }

        return new EjbModule(ejbJar);

    }

    private boolean isSingleton(Class<?> clazz)
    {
        return clazz.isAnnotationPresent(Singleton.class) ? true : false;
    }

    private boolean isStateless(Class<?> clazz)
    {
        return clazz.isAnnotationPresent(Stateless.class) ? true : false;
    }

    private boolean isStatefull(Class<?> clazz)
    {
        return clazz.isAnnotationPresent(Stateful.class) ? true : false;
    }
}
