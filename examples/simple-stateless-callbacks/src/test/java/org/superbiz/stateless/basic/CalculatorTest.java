/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.stateless.basic;

import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CalculatorTest implements ExecutionObserver {

    private static final String JNDI = "java:global/simple-stateless-callbacks/CalculatorBean";

    private List<Object> received = new ArrayList<Object>();

    public Context getContext() throws NamingException {
        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        return new InitialContext(p);

    }

    @Test
    public void test() throws Exception {
        ExecutionChannel.getInstance().addObserver(this);

        final EJBContainer container = EJBContainer.createEJBContainer();

        {
            final CalculatorBean calculator = (CalculatorBean) getContext().lookup(JNDI);

            Assert.assertEquals(10, calculator.add(4, 6));

            //the bean is constructed only when it is used for the first time
            Assert.assertEquals("postConstruct", this.received.remove(0));
            Assert.assertEquals("add", this.received.remove(0));

            Assert.assertEquals(-2, calculator.subtract(4, 6));
            Assert.assertEquals("subtract", this.received.remove(0));

            Assert.assertEquals(24, calculator.multiply(4, 6));
            Assert.assertEquals("multiply", this.received.remove(0));

            Assert.assertEquals(2, calculator.divide(12, 6));
            Assert.assertEquals("divide", this.received.remove(0));

            Assert.assertEquals(4, calculator.remainder(46, 6));
            Assert.assertEquals("remainder", this.received.remove(0));
        }

        {
            final CalculatorBean calculator = (CalculatorBean) getContext().lookup(JNDI);

            Assert.assertEquals(10, calculator.add(4, 6));
            Assert.assertEquals("add", this.received.remove(0));

        }

        container.close();
        Assert.assertEquals("preDestroy", this.received.remove(0));
        Assert.assertTrue(this.received.isEmpty());
    }

    @Override
    public void onExecution(Object value) {
        System.out.println("Test step -> " + value);
        this.received.add(value);
    }
}
