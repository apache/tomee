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

import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.TransactionType;
import org.junit.runner.RunWith;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.interceptor.AroundInvoke;

/**
 * @version $Rev: 964531 $ $Date: 2010-07-15 14:40:36 -0400 (Thu, 15 Jul 2010) $
 */
@RunWith(ValidationRunner.class)
public class CheckInvalidTransactionAttributeTest {
    @Keys(@Key(value = "xml.invalidTransactionAttribute", type = KeyType.WARNING))
    public EjbJar xml() throws SecurityException, NoSuchMethodException {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(CheeseEjb.class));
        bean.setTransactionType(TransactionType.BEAN);
        final ContainerTransaction tx = new ContainerTransaction();
        tx.getMethod().add(new Method(bean.getEjbName(), CheeseEjb.class.getMethod("sayCheesePlease", null)));
        tx.setTransAttribute(TransAttribute.REQUIRED);
        ejbJar.getAssemblyDescriptor().getContainerTransaction().add(tx);
        return ejbJar;
    }

    @Keys(@Key(value = "ann.invalidTransactionAttribute", type = KeyType.WARNING))
    public EjbJar annotation() throws SecurityException, NoSuchMethodException {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(AnnotatedCheeseEjb.class));
        return ejbJar;
    }

    private static class CheeseEjb {
        @AroundInvoke
        // need to add this to cause validation to fail. Validation does not fail on warnings, which causes this framework to not work properly
        public void sayCheese() {
        }

        public void sayCheesePlease() {
        }
    }

    @TransactionManagement(TransactionManagementType.BEAN)
    private static class AnnotatedCheeseEjb {
        @AroundInvoke
        // need to add this to cause validation to fail. Validation does not fail on warnings, which causes this framework to not work properly
        public void sayCheese() {
        }

        @TransactionAttribute(TransactionAttributeType.REQUIRED)
        public void sayCheesePlease() {
        }
    }
}
