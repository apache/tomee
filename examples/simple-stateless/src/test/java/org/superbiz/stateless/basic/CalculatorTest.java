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

import junit.framework.TestCase;

import javax.ejb.embeddable.EJBContainer;

public class CalculatorTest extends TestCase {

    private CalculatorBean calculator;

    /**
     * Bootstrap the Embedded EJB Container
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {

        EJBContainer ejbContainer = EJBContainer.createEJBContainer();

        Object object = ejbContainer.getContext().lookup("java:global/simple-stateless/CalculatorBean");

        assertTrue(object instanceof CalculatorBean);

        calculator = (CalculatorBean) object;
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

        assertEquals(-2, calculator.subtract(4, 6));

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

}
