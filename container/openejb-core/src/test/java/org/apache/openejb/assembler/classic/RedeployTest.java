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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Connector;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.test.stateful.AnnotatedFieldInjectionStatefulBean;
import org.apache.openejb.test.stateful.EncStatefulHome;
import org.apache.openejb.test.stateful.EncStatefulObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class RedeployTest extends TestCase {
    public void test() throws Exception {
        // create reference to openejb itests
        File file = new File(System.getProperty("user.home") + "/.m2/repository/org/apache/openejb/openejb-itests-beans/3.0-incubating-SNAPSHOT/openejb-itests-beans-3.0-incubating-SNAPSHOT.jar");
        if (!file.canRead()) return;

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createConnectionManager(config.configureService(ConnectionManagerInfo.class));

        // managed JDBC
        assembler.createConnector(config.configureService(ConnectorInfo.class));

        // unmanaged JDBC
        Connector connector = new Connector();
        connector.setId("Default Unmanaged JDBC Database");
        connector.setProvider("Default Unmanaged JDBC Database");
        assembler.createConnector(config.configureService(connector, ConnectorInfo.class));

        // JMS
        Resource resource = new Resource();
        resource.setId("Default JMS Resource Adapter");
        resource.setProvider("Default JMS Resource Adapter");
        assembler.createResource(config.configureService(resource, ResourceInfo.class));

        connector = new Connector();
        connector.setId("Default JMS Connection Factory");
        connector.setProvider("Default JMS Connection Factory");
        assembler.createConnector(config.configureService(connector, ConnectorInfo.class));

        // containers
        assembler.createContainer(config.configureService(BmpEntityContainerInfo.class));
        assembler.createContainer(config.configureService(CmpEntityContainerInfo.class));
        assembler.createContainer(config.configureService(StatefulSessionContainerInfo.class));
        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));
        assembler.createContainer(config.configureService(MdbContainerInfo.class));

        createAndDestroy(assembler, config, file);
        createAndDestroy(assembler, config, file);
        createAndDestroy(assembler, config, file);
    }

    private void createAndDestroy(Assembler assembler, ConfigurationFactory config, File file) throws Exception {
        assembler.createApplication(config.configureApplication(file));

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        InitialContext ctx = new InitialContext(properties);
        EncStatefulHome home = (EncStatefulHome) ctx.lookup(AnnotatedFieldInjectionStatefulBean.class.getSimpleName());
        EncStatefulObject ejbObject = home.create("foo");
        ejbObject.lookupStringEntry();

        assembler.destroyApplication(file.getAbsolutePath());

        try {
            ejbObject.lookupStringEntry();
            fail("Proxy should no longer be valid");
        } catch (Exception e) {
            // this should happen
        }

        try {
            ctx.lookup(AnnotatedFieldInjectionStatefulBean.class.getSimpleName());
            fail("JNDI References should have been cleaned up");
        } catch (NamingException e) {
            // this also should happen
        }
    }

}
