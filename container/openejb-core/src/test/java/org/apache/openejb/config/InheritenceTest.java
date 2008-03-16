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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.Init;
import javax.ejb.Remove;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;
import javax.sql.DataSource;
import java.rmi.RemoteException;

/**
 * @version $Rev$ $Date$
 */
public class InheritenceTest extends TestCase {

    public void testNoTest(){}

    public void _test() throws Exception {
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createContainer(config.configureService(StatefulSessionContainerInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Color.class));
        ejbJar.addEnterpriseBean(new StatefulBean(Red.class));
        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        StatefulBeanInfo expected = (StatefulBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        StatefulBeanInfo actual = (StatefulBeanInfo) ejbJarInfo.enterpriseBeans.get(1);


        assertEquals("businessLocal", expected.businessLocal, actual.businessLocal);
        assertEquals("businessRemote", expected.businessRemote, actual.businessRemote);
        assertEquals("local", expected.local, actual.local);
        assertEquals("localHome", expected.localHome, actual.localHome);
        assertEquals("remote", expected.remote, actual.remote);
        assertEquals("home", expected.home, actual.home);

        assertEquals("removeMethods", expected.removeMethods, actual.removeMethods);
        assertEquals("initMethods", expected.initMethods, actual.initMethods);

        assertEquals("postActivate", expected.postActivate, actual.postActivate);
        assertEquals("prePassivate", expected.prePassivate, actual.prePassivate);

        assertEquals("postConstruct", expected.postConstruct, actual.postConstruct);
        assertEquals("preDestroy", expected.preDestroy, actual.preDestroy);

    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    public static interface ColorLocal2 {
    }

    public static interface ColorRemote2 {
    }

    public static interface ColorEjbHome extends EJBHome {
        ColorEjbObject create() throws CreateException, RemoteException;
    }

    public static interface ColorEjbObject extends EJBObject {
    }

    public static interface ColorEjbLocalHome extends EJBLocalHome {
        ColorEjbLocalObject create() throws CreateException;
    }

    public static interface ColorEjbLocalObject extends EJBLocalObject {
    }


    @EJB(name = "colorClassEjb", beanInterface = ColorLocal.class)
    @Resource(name = "colorClassResource", type = DataSource.class)
    @RolesAllowed({"ColorManager"})
    @TransactionManagement(TransactionManagementType.BEAN)
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Local({ColorLocal2.class})
    @Remote({ColorRemote2.class})
    @LocalHome(ColorEjbLocalHome.class)
    @RemoteHome(ColorEjbHome.class)
    public static class Color implements ColorLocal, ColorRemote {

        @EJB
        private ColorRemote colorFieldEjb;

        @Resource
        private DataSource colorFieldResource;

        public void methodOne() {
        }

        public void methodTwo() {
        }

        public void methodThree() {
        }

        public void methodFour() {
        }

        @PostConstruct
        private void colorPostConstruct() {

        }

        @PreDestroy
        private void colorPreDestroy() {
        }

        @Init
        public void colorInit(){
        }

        @Remove
        public void colorRemove(){

        }

        @PrePassivate
        public void colorPrePassivate(){
        }

        @PostActivate
        public void colorPostActivate(){

        }
    }

    public static class Red extends Color {
    }
}
