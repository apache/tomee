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
package org.apache.openejb.cdi;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateful;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class StatefulDependentInjectionTest {


    @EJB
    private OrangeStateful orange;

    @Test
    public void test() throws Exception {

        assertNotNull(orange);

        try {
            orange.someBusinessMethod();

            fail("call should not be allowed");
        } catch (final EJBException e) {
            assertTrue(IllegalStateException.class.isInstance(e.getCause()));
        }
    }

    @Module
    public SessionBean getEjbs() {
        return new SingletonBean(OrangeStateful.class);
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(Peeler.class);
        return beans;
    }


    @Stateful
    public static class OrangeStateful {

        @Produces
        @Resource
        private SessionContext sessionContext;

        @Inject
        private Peeler peeler;

        public void someBusinessMethod() {

            peeler.peel();

        }
    }

    public static class Peeler {

        @Inject
        private SessionContext sessionContext;

        public void peel() {
            if (!sessionContext.isCallerInRole("worker")) {
                throw new IllegalStateException("denied");
            }

            // do the work
        }
    }

}
