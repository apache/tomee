/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.threads;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.security.Principal;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class SecurityPropagationTest {
    @Module
    public EnterpriseBean bean() {
        return new StatelessBean(ExecutorBean.class).localBean();
    }

    @EJB
    private ExecutorBean bean;

    @Test
    public void checkItIsTrue() throws Exception {
        bean.submit(new RunnableTest()).get();
    }

    @Stateless
    @RunAs("tomee")
    public static class ExecutorBean {
        @Resource
        private ManagedExecutorService executorService;

        @Resource
        private EJBContext ejbContext;

        public Future<?> submit(final RunnableTest task) throws NamingException {
            final Principal principal = ejbContext.getCallerPrincipal();
            task.setExpectedPrincipal(principal);
            return executorService.submit(task);
        }
    }

    public static class RunnableTest implements Runnable {
        private Principal expectedPrincipal;

        public void setExpectedPrincipal(Principal expectedPrincipal) {
            this.expectedPrincipal = expectedPrincipal;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            final InitialContext initialContext;
            try {
                initialContext = new InitialContext();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            final EJBContext ejbContext;
            try {
                ejbContext = (SessionContext) initialContext.lookup("java:comp/EJBContext");
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }

            final Principal callerPrincipal = ejbContext.getCallerPrincipal();
            if (expectedPrincipal != null) {
                if (!expectedPrincipal.equals(callerPrincipal)) {
                    throw new IllegalStateException("the caller principal " + callerPrincipal + " is not the expected " + expectedPrincipal);
                }
            } else {
                if (callerPrincipal != null) {
                    throw new IllegalStateException("the caller principal " + callerPrincipal + " is not the expected " + expectedPrincipal);
                }
            }
        }

    }
}
