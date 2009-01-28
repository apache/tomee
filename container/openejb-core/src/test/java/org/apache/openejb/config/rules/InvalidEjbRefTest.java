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

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import static org.apache.openejb.config.rules.ValidationAssertions.assertFailures;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class InvalidEjbRefTest extends TestCase {

    private ConfigurationFactory config;

    public void test() throws Exception {

        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(EjbRefBean.class));

        StatelessBean fooBean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        fooBean.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        fooBean.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        fooBean.addBusinessLocal(FooLocal.class.getName());
        fooBean.addBusinessRemote(FooRemote.class.getName());

        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("ann.ejb.ejbObject");
        expectedKeys.add("ann.ejb.ejbLocalObject");
        expectedKeys.add("ann.ejb.beanClass");
        expectedKeys.add("ann.ejb.notInterface");

        try {
            config.configureApplication(ejbJar);
            fail("A ValidationFailedException should have been thrown");
        } catch (ValidationFailedException e) {
            assertFailures(expectedKeys, e);
        }
    }

    public void setUp() throws Exception {
        config = new ConfigurationFactory(true);
        ContainerSystemInfo containerSystem = config.getOpenEjbConfiguration().containerSystem;
        containerSystem.containers.add(config.configureService(StatelessSessionContainerInfo.class));
    }


    public static class EjbRefBean implements EjbRefBeanLocal {
        // invalid
        @EJB
        private FooBean fooBean;

        // valid
        @EJB
        private FooEJBHome fooEJBHome;

        // invalid
        @EJB
        private FooEJBObject fooEJBObject;

        // valid
        @EJB
        private FooEJBLocalHome fooEJBLocalHome;

        // invalid
        @EJB
        private FooEJBLocalObject fooEJBLocalObject;

        // valid
        @EJB
        private FooRemote fooRemote;

        // valid
        @EJB
        private FooLocal fooLocal;

        // valid
        @EJB
        private FooUnknown fooUnknown;

        // invalid
        @EJB
        private FooClass fooClass;
    }

    public static interface EjbRefBeanLocal {

    }

    public static class FooBean {

    }

    public static interface FooEJBHome extends EJBHome {
        FooEJBObject create();
    }

    public static interface FooEJBObject extends EJBObject {
    }

    public static interface FooEJBLocalHome extends EJBLocalHome {
        FooEJBLocalObject create();
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