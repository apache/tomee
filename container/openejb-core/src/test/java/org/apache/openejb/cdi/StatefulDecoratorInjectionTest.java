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
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.ejb.ApplicationException;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.security.Principal;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class StatefulDecoratorInjectionTest {


    @EJB
    private OrangeStateful orange;

    @Test
    public void test() throws Exception {

        assertNotNull(orange);

        try {
            orange.someBusinessMethod();

            fail("call should not be allowed");
        } catch (AccessDeniedException e) {
            // pass
        }
    }

    @Module
    public SessionBean getEjbs() {
        return new SingletonBean(OrangeStatefulBean.class);
    }

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addDecorator(OrangeSecurity.class);
        return beans;
    }

    @Local
    public static interface OrangeStateful {

        public void someBusinessMethod();
    }

    @Stateful
    public static class OrangeStatefulBean implements OrangeStateful {

        @Produces
        @Resource
        private javax.ejb.SessionContext sessionContext;

        public void someBusinessMethod() {

            // do work
        }
    }

    @Decorator
    public static class OrangeSecurity implements OrangeStateful {

        @Inject
        private SessionContext sessionContext;

        @Inject
        @Delegate
        private OrangeStateful orangeStateful;

        @Override
        public void someBusinessMethod() {
            if (!sessionContext.isCallerInRole("worker")) {
                throw new AccessDeniedException(sessionContext.getCallerPrincipal());
            }

            orangeStateful.someBusinessMethod();
        }
    }

    @ApplicationException
    public static class AccessDeniedException extends RuntimeException {

        private final Principal principal;

        public AccessDeniedException(Principal principal) {
            this.principal = principal;
        }

        public Principal getPrincipal() {
            return principal;
        }
    }
}
