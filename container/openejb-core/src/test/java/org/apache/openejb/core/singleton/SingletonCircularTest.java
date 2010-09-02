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
package org.apache.openejb.core.singleton;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;

/**
 * @version $Rev$ $Date$
 */
public class SingletonCircularTest extends TestCase {
    
    private static final String one = "one";
    private static final String two = "two";

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));
        
        actual.clear();

        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(One.class));

        // startup and trigger @PostConstruct
        assembler.createApplication(config.configureApplication(ejbJar));

        assertTrue(one, actual.contains(one));
        assertTrue(two, actual.contains(two));
    }

    private final static List<String> actual = new ArrayList<String>();

    @Singleton
    @Startup
    public static class One {

        @EJB Two two;
        
        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(one);
        }
    }

    @Singleton
    @Startup
    public static class Two {

        @EJB One one;
        
        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(two);
        }
    }
   
}
