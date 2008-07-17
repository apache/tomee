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
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;

/**
 * @version $Rev$ $Date$
 */
public class InvalidInterfacesTest extends TestCase {
    private ConfigurationFactory config;

    public void testBadHomeAndLocal() throws Exception {

        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndLocal(FooLocal.class, FooLocal.class);
        bean.setHomeAndRemote(FooLocal.class, FooLocal.class);

        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            for (ValidationFailure failure : e.getFailures()) {
                System.out.println("failure = " + failure.getMessageKey());
            }
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

}
