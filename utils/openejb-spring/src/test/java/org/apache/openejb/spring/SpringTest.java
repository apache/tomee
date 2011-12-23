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

import java.util.Map;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openejb.Container;
import org.apache.openejb.core.entity.EntityContainer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Debug;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTest extends TestCase {
    public void test() throws Exception {
        System.setProperty("openejb.deployments.classpath.include",".*/openejb-spring.*");

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("org/apache/openejb/spring/spring.xml");
        
        assertTrue(context.getBeanDefinitionCount() > 0);

        //
        // OpenEJB
        //
        Map map = context.getBeansOfType(OpenEJB.class);
        assertEquals("OpenEJB instance count", 1, map.size());
        OpenEJB openEJB = (OpenEJB) map.values().iterator().next();
        assertNotNull("openEJB is null", openEJB);
        assertTrue(openEJB.isStarted());
        assertTrue(org.apache.openejb.OpenEJB.isInitialized());

        //
        // ContainerSystem
        //
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        assertNotNull("containerSystem is null", containerSystem);
        Context initialContext = containerSystem.getJNDIContext();
        assertNotNull(initialContext);

        //
        // TransactionManager
        //
        Object tm = context.getBean("tm");
        TransactionManager springTM = (TransactionManager) tm;
        assertNotNull("springTM is null", springTM);
        assertTrue("springTM should be an instance of MockTransactionManager", springTM instanceof MockTransactionManager);

        TransactionManager systemTM = SystemInstance.get().getComponent(TransactionManager.class);
        assertNotNull("systemTM is null", systemTM);
        assertSame(springTM, systemTM);

        assertSame(springTM, initialContext.lookup("openejb/TransactionManager"));

        //
        // SecurityService
        //
        SecurityService springSecurityService = (SecurityService) context.getBean("sec");
        assertNotNull("springSecurityService is null", springSecurityService);
        assertTrue("springSecurityService should be an instance of MockSecurityService", springSecurityService instanceof MockSecurityService);

        SecurityService systemSecurityService = SystemInstance.get().getComponent(SecurityService.class);
        assertNotNull("systemSecurityService is null", systemSecurityService);
        assertSame(springSecurityService, systemSecurityService);

        assertSame(springSecurityService, initialContext.lookup("openejb/SecurityService"));

        //
        // DataSource
        //
        DataSource springDataSource = (DataSource) context.getBean("ds");
        assertNotNull("springDataSource is null", springDataSource);
        assertTrue("springDataSource should be an instance of BasicDataSource", springDataSource instanceof BasicDataSource);

        DataSource jndiDataSource = (DataSource) initialContext.lookup("openejb/Resource/ds");
        assertSame(springDataSource, jndiDataSource);

        DataSource exportedDS = (DataSource) context.getBean("openejbDS");
        assertSame(springDataSource, exportedDS);

        //
        // Container
        //
        Container singletonContainer = containerSystem.getContainer("SpringDefinedSingletonContainer");
        assertTrue("singletonContainer should be an instance of SingletonContainer", singletonContainer instanceof org.apache.openejb.core.singleton.SingletonContainer);
        Container statelessContainer = containerSystem.getContainer("SpringDefinedStatelessContainer");
        assertTrue("statelessContainer should be an instance of StatelessContainer", statelessContainer instanceof org.apache.openejb.core.stateless.StatelessContainer);
        Container statefulContainer = containerSystem.getContainer("SpringDefinedStatefulContainer");
        assertTrue("statefulContainer should be an instance of StatefulContainer", statefulContainer instanceof org.apache.openejb.core.stateful.StatefulContainer);
        Container mdbContainer = containerSystem.getContainer("SpringDefinedMdbContainer");
        assertTrue("mdbContainer should be an instance of MdbContainer", mdbContainer instanceof org.apache.openejb.core.mdb.MdbContainer);
        Container bmpContainer = containerSystem.getContainer("SpringDefinedBmpContainer");
        assertTrue("bmpContainer should be an instance of BmpContainer", bmpContainer instanceof EntityContainer);
        Container cmpContainer = containerSystem.getContainer("SpringDefinedCmpContainer");
        assertTrue("cmpContainer should be an instance of CmpContainer", cmpContainer instanceof org.apache.openejb.core.cmp.CmpContainer);

        //
        // JNDI Context
        //
        System.out.println();
        System.out.println();
        Debug.printContext(initialContext);
        System.out.println();
        System.out.println();

        //
        // EJB with Spring bean injected
        //
        Echo echo = (Echo) context.getBean("EchoBeanLocal");
        assertNotNull("echo is null", echo);
        assertEquals("Hello MySpringBean", echo.echo("Hello"));
//        echo = (Echo) context.getBean("Echo");
//        assertNotNull("echo is null", echo);
//        assertEquals("Hello MySpringBean", echo.echo("Hello"));

        //
        // Spring bean with EJB injected
        //
        EchoReverseBean echoReverse = (EchoReverseBean) context.getBean("EchoReverse");
        assertNotNull("echoReverse is null", echoReverse);
        assertEquals("naeBgnirpSyM olleH", echoReverse.echoReverse("Hello"));
//        echoReverse = (EchoReverseBean) context.getBean("Echo");
//        assertNotNull("echoReverse is null", echoReverse);
//        assertEquals("Hello MySpringBean", echoReverse.echoReverse("Hello"));

        //
        // Stop the Spring Context
        //
        context.destroy();
    }
}
