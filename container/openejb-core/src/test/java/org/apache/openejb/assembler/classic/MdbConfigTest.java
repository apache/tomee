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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.test.mdb.BasicMdbBean;

import javax.jms.MessageListener;

/**
 * @version $Rev$ $Date$
 */
public class MdbConfigTest extends TestCase {
    public void test() throws Exception {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // JMS
        assembler.createResource(config.configureService("Default JMS Resource Adapter", ResourceInfo.class));

        // containers
        MdbContainerInfo mdbContainerInfo = config.configureService(MdbContainerInfo.class);
        assembler.createContainer(mdbContainerInfo);

        // add fake mdb container
        ContainerInfo containerInfo = config.configureService(MdbContainerInfo.class);
        containerInfo.id = "FakeContainer";
        containerInfo.displayName = "Fake Container";
        containerInfo.properties.setProperty("MessageListenerInterface", "java.lang.Runnable");
        assembler.createContainer(containerInfo);

        // generate ejb jar application
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(createJaxbMdb("JmsMdb", BasicMdbBean.class.getName(), MessageListener.class.getName()));
        ejbJar.addEnterpriseBean(createJaxbMdb("FakeMdb", FakeMdb.class.getName(), Runnable.class.getName()));
        EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "FakeEjbJar", "fake.jar", ejbJar, null);

        // configure and deploy it
        EjbJarInfo info = config.configureApplication(ejbModule);
        assembler.createEjbJar(info);
    }

    public static class FakeMdb implements Runnable {
        public void run() {
        }
    }

    private MessageDrivenBean createJaxbMdb(String ejbName, String mdbClass, String messageListenerInterface) {
        MessageDrivenBean bean = new MessageDrivenBean(ejbName);
        bean.setEjbClass(mdbClass);
        bean.setMessagingType(messageListenerInterface);

        ActivationConfig activationConfig = new ActivationConfig();
        activationConfig.getActivationConfigProperty().add(new ActivationConfigProperty("destination", ejbName));
        activationConfig.getActivationConfigProperty().add(new ActivationConfigProperty("destinationType", "javax.jms.Queue"));
        bean.setActivationConfig(activationConfig);

        return bean;
    }
}
