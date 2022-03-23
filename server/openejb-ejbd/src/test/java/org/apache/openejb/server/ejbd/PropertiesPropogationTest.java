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
package org.apache.openejb.server.ejbd;

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.proxy.InvocationHandler;
import org.apache.openejb.client.proxy.ProxyManager;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesPropogationTest extends TestCase {

    public void test() throws Exception {
        EjbServer ejbServer = new EjbServer();

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(ejbServer, 10);
        ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();

        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        ConfigurationFactory config = new ConfigurationFactory();

        EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));
        EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);
        deployment.getProperties().put("color", "orange");
        deployment.getProperties().put("openejb.client.color", "red");

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);
        EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);

        assertTrue(beanInfo.properties.containsKey("color"));
        assertTrue(beanInfo.properties.containsKey("openejb.client.color"));
        assertEquals("orange", beanInfo.properties.get("color"));
        assertEquals("red", beanInfo.properties.get("openejb.client.color"));

        assembler.createApplication(ejbJarInfo);

        ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        BeanContext info = cs.getBeanContext("WidgetBean");
        assertNotNull(info);

        assertTrue(info.getProperties().containsKey("color"));
        assertTrue(info.getProperties().containsKey("openejb.client.color"));

        assertEquals("orange", info.getProperties().get("color"));
        assertEquals("red", info.getProperties().get("openejb.client.color"));

        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
        Context context = new InitialContext(props);

        Widget remote = (Widget) context.lookup("WidgetBeanRemote");

        InvocationHandler handler = ProxyManager.getInvocationHandler(remote);

        EJBObjectHandler objectHandler = EJBObjectHandler.class.cast(handler);

        Properties properties = objectHandler.getEjb().getProperties();

        // Should only contain "openejb.client.*" properties
        assertFalse(properties.containsKey("color"));

        // The openejb.client.color property should have been propogated
        assertTrue(properties.containsKey("openejb.client.color"));
        assertEquals("red", properties.getProperty("openejb.client.color"));

        serviceDaemon.stop();
        OpenEJB.destroy();
    }

    @Remote
    public static interface Widget {

        public Object echo(Object o);
    }

    public static class WidgetBean implements Widget {

        public Object echo(Object o) {
            return o;
        }
    }
}
