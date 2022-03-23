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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.MessageDestinationUsage;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.junit.AfterClass;
import org.junit.Assert;

import jakarta.jms.Queue;
import javax.naming.NamingException;
import java.util.List;

public class MessageDestinationTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final ClassLoader cl = this.getClass().getClassLoader();
        final AppModule app = new AppModule(cl, "app");

        final MessageDestinationRef webMessageDestRef = new MessageDestinationRef();
        webMessageDestRef.setMessageDestinationRefName("jms/myqueue");
        webMessageDestRef.setMessageDestinationType("jakarta.jms.Queue");
        webMessageDestRef.setMessageDestinationUsage(MessageDestinationUsage.PRODUCES);
        webMessageDestRef.setMessageDestinationLink("ejb.jar#myqueue");

        final WebApp webApp = new WebApp();
        webApp.setMetadataComplete(true);
        webApp.getMessageDestinationRef().add(webMessageDestRef);
        final WebModule webModule = new WebModule(webApp, "web", cl, "war", "web");

        app.getWebModules().add(webModule);

        final EjbJar ejbJar = new EjbJar();
        final StatelessBean statelessBean = new StatelessBean(GreenBean.class);
        final MessageDestinationRef ejbMessageDestRef = new MessageDestinationRef();
        ejbMessageDestRef.setMessageDestinationRefName("jms/myqueue");
        ejbMessageDestRef.setMessageDestinationType("jakarta.jms.Queue");
        ejbMessageDestRef.setMessageDestinationUsage(MessageDestinationUsage.PRODUCES);
        ejbMessageDestRef.setMessageDestinationLink("myqueue");

        statelessBean.getMessageDestinationRef().add(ejbMessageDestRef);
        ejbJar.addEnterpriseBean(statelessBean);

        final MessageDestination queue = new MessageDestination();
        queue.setMessageDestinationName("myqueue");
        ejbJar.getAssemblyDescriptor().getMessageDestination().add(queue);

        final EjbModule ejbModule = new EjbModule(cl, "ejb.jar", ejbJar, new OpenejbJar());
        app.getEjbModules().add(ejbModule);


        final AppInfo info = config.configureApplication(app);
        assembler.createApplication(info);

        final Object beanQueue = assembler.getContainerSystem().getBeanContext("GreenBean").getJndiContext().lookup("comp/env/jms/myqueue");
        Assert.assertTrue(beanQueue instanceof Queue);

        Assert.assertEquals(1, info.webApps.size());
        final List<ResourceEnvReferenceInfo> resourceEnvRefs = info.webApps.get(0).jndiEnc.resourceEnvRefs;

        boolean found = false;
        for (final ResourceEnvReferenceInfo resourceEnvRef : resourceEnvRefs) {
            if (! "comp/env/jms/myqueue".equals(resourceEnvRef.referenceName)) continue;

            found = true;
            Assert.assertEquals("jms/myqueue", resourceEnvRef.resourceID);
            Assert.assertEquals("jakarta.jms.Queue", resourceEnvRef.resourceEnvRefType);
        }

        Assert.assertTrue(found);
    }

    public static interface Color {
        public void test() throws NamingException;
    }

    public static class GreenBean implements Color {
        public void test() throws NamingException {
        }
    }
}
