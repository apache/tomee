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
package org.apache.openejb.config.rules;

import static org.apache.openejb.util.Join.join;
import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.OpenEJBException;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class InvalidInterfacesTest extends TestCase {
    private ConfigurationFactory config;

    public void testCorrectInterfaces() throws Exception {

        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        bean.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        bean.addBusinessLocal(FooLocal.class.getName());
        bean.addBusinessRemote(FooRemote.class.getName());

        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            for (ValidationFailure failure : e.getFailures()) {
                System.out.println("failure = " + failure.getMessageKey());
            }
            fail("There should be no validation failures");
        }

    }

    public void testBusinessLocal() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.businessLocal");
        expectedKeys.add("xml.home.businessLocal");
        expectedKeys.add("xml.local.businessLocal");
        expectedKeys.add("xml.localHome.businessLocal");

        validate(FooLocal.class, expectedKeys);
    }

    public void testBusinessRemote() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.businessRemote");
        expectedKeys.add("xml.home.businessRemote");
        expectedKeys.add("xml.local.businessRemote");
        expectedKeys.add("xml.localHome.businessRemote");

        validate(FooRemote.class, expectedKeys);
    }

    public void testEJBObject() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.home.ejbObject");
        expectedKeys.add("xml.local.ejbObject");
        expectedKeys.add("xml.localHome.ejbObject");
        expectedKeys.add("xml.businessLocal.ejbObject");
        expectedKeys.add("xml.businessRemote.ejbObject");

        validate(FooEJBObject.class, expectedKeys);
    }

    public void testEJBHome() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.ejbHome");
        expectedKeys.add("xml.local.ejbHome");
        expectedKeys.add("xml.localHome.ejbHome");
        expectedKeys.add("xml.businessLocal.ejbHome");
        expectedKeys.add("xml.businessRemote.ejbHome");

        validate(FooEJBHome.class, expectedKeys);
    }

    public void testEJBLocalHome() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.ejbLocalHome");
        expectedKeys.add("xml.home.ejbLocalHome");
        expectedKeys.add("xml.local.ejbLocalHome");
        expectedKeys.add("xml.businessLocal.ejbLocalHome");
        expectedKeys.add("xml.businessRemote.ejbLocalHome");

        validate(FooEJBLocalHome.class, expectedKeys);
    }

    public void testEJBLocalObject() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.ejbLocalObject");
        expectedKeys.add("xml.home.ejbLocalObject");
        expectedKeys.add("xml.localHome.ejbLocalObject");
        expectedKeys.add("xml.businessLocal.ejbLocalObject");
        expectedKeys.add("xml.businessRemote.ejbLocalObject");

        validate(FooEJBLocalObject.class, expectedKeys);
    }

    public void testUnkown() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.unknown");
        expectedKeys.add("xml.home.unknown");
        expectedKeys.add("xml.localHome.unknown");
        expectedKeys.add("xml.local.unknown");

        validate(FooUnknown.class, expectedKeys);
    }

    public void testBeanClass() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.beanClass");
        expectedKeys.add("xml.home.beanClass");
        expectedKeys.add("xml.localHome.beanClass");
        expectedKeys.add("xml.local.beanClass");
        expectedKeys.add("xml.businessRemote.beanClass");
        expectedKeys.add("xml.businessLocal.beanClass");

        validate(FooBean.class, expectedKeys);
    }

    public void testNotInterface() throws Exception {

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("xml.remote.notInterface");
        expectedKeys.add("xml.home.notInterface");
        expectedKeys.add("xml.localHome.notInterface");
        expectedKeys.add("xml.local.notInterface");
        expectedKeys.add("xml.businessRemote.notInterface");
        expectedKeys.add("xml.businessLocal.notInterface");

        validate(FooClass.class, expectedKeys);
    }

    private void validate(Class interfaceClass, List<String> expectedKeys) throws OpenEJBException {

        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndLocal(interfaceClass, interfaceClass);
        bean.setHomeAndRemote(interfaceClass, interfaceClass);
        bean.addBusinessLocal(interfaceClass);
        bean.addBusinessRemote(interfaceClass);

        try {
            config.configureApplication(ejbJar);
            fail("A ValidationFailedException should have been thrown");
        } catch (ValidationFailedException e) {
            ValidationAssertions.assertFailures(expectedKeys, e);
        }
    }

    public void setUp() throws Exception {
        config = new ConfigurationFactory(true);
        ContainerSystemInfo containerSystem = config.getOpenEjbConfiguration().containerSystem;
        containerSystem.containers.add(config.configureService(StatelessSessionContainerInfo.class));
    }

    public static class FooBean {

    }

    public static interface FooEJBHome extends EJBHome {
    }

    public static interface FooEJBObject extends EJBObject {
    }

    public static interface FooEJBLocalHome extends EJBLocalHome {
    }

    public static interface FooEJBLocalObject extends EJBLocalObject {
    }

    @Remote
    public static interface FooRemote {
    }

    @Local
    public static interface FooLocal {
    }

    public static interface FooUnknown {
    }

    public static class FooClass {

    }
}
