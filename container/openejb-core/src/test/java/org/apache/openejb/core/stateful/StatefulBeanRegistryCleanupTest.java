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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.OpenEJBInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertTrue;

/**
 *
 * @version $Rev$ $Date$
 */
public class StatefulBeanRegistryCleanupTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, OpenEJBInitialContextFactory.class.getName());
//        System.setProperty("openejb.validation.output.level" , "VERBOSE");

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        assembler.createContainer(statefulContainerInfo);

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(MyBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final Context context = new InitialContext();
        final MyBeanInterface myBean = (MyBeanInterface) context.lookup("MyBeanRemote");
        java.lang.reflect.Field hField = myBean.getClass().getSuperclass().getDeclaredField("h");
        hField.setAccessible(true);
        Object hValue = hField.get(myBean);
        ConcurrentMap reg = ((StatefulEjbObjectHandler) hValue).getLiveHandleRegistry();

        myBean.cleanup();
        assertTrue("Live handle registry should be empty after removal", reg.isEmpty());
    }

    public interface MyBeanInterface {
        String echo(String string);

        @Remove
        void cleanup();
    }


    @Stateful
    @Remote
    public static class MyBean implements MyBeanInterface {

        @Override
        public String echo(final String string) {
            final StringBuilder sb = new StringBuilder(string);
            return sb.reverse().toString();
        }

        @Override
        public void cleanup() {
            System.out.println("cleaning up MyBean instance");
        }

    }
}
