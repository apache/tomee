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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.stateless.basic;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    private static EJBContainer ejbContainer;

    private CalculatorBean calculator;

    @BeforeClass
    public static void startTheContainer() {
        ejbContainer = EJBContainer.createEJBContainer();
    }

    @Before
    public void lookupABean() throws NamingException {
        Object object = ejbContainer.getContext().lookup("java:global/simple-stateless/CalculatorBean");

        assertTrue(object instanceof CalculatorBean);

        calculator = (CalculatorBean) object;
    }

    @AfterClass
    public static void stopTheContainer() {
        if (ejbContainer != null) {
            ejbContainer.close();
        }
    }

    /**
     * Test Add method
     */
    @Test
    public void testAdd() {

        assertEquals(10, calculator.add(4, 6));

    }

    /**
     * Test Subtract method
     */
    @Test
    public void testSubtract() {

        assertEquals(-2, calculator.subtract(4, 6));

    }

    /**
     * Test Multiply method
     */
    @Test
    public void testMultiply() {

        assertEquals(24, calculator.multiply(4, 6));

    }

    /**
     * Test Divide method
     */
    @Test
    public void testDivide() {

        assertEquals(2, calculator.divide(12, 6));

    }

    /**
     * Test Remainder method
     */
    @Test
    public void testRemainder() {

        assertEquals(4, calculator.remainder(46, 6));

    }

}
