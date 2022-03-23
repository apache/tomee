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
package org.superbiz.cdi.decorators;

import junit.framework.TestCase;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.embeddable.EJBContainer;
import java.util.concurrent.Callable;

public class CalculatorTest extends TestCase {

    @EJB
    private Calculator calculator;

    @EJB
    private ManagerBean manager;

    /**
     * Bootstrap the Embedded EJB Container
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        EJBContainer.createEJBContainer().getContext().bind("inject", this);
    }

    /**
     * Test Add method
     */
    public void testAdd() {

        assertEquals(10, calculator.add(4, 6));

    }

    /**
     * Test Subtract method
     */
    public void testSubtract() {

        try {
            calculator.subtract(4, 6);

            fail("AccessDeniedException should have been thrown for unauthenticated access");
        } catch (AccessDeniedException expected) {
            // pass
        }

        final int result = manager.call(new Callable<Integer>() {
            public Integer call() {
                return calculator.subtract(4, 6);
            }
        });

        assertEquals(-2, result);

    }

    /**
     * Test Multiply method
     */
    public void testMultiply() {

        assertEquals(24, calculator.multiply(4, 6));

    }

    /**
     * Test Divide method
     */
    public void testDivide() {

        assertEquals(2, calculator.divide(12, 6));

    }

    /**
     * Test Remainder method
     */
    public void testRemainder() {

        assertEquals(4, calculator.remainder(46, 6));

    }

    @Stateless
    @RunAs("Manager")
    public static class ManagerBean {

        public <V> V call(Callable<V> callable) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
