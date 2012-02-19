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
package org.superbiz.calculator.ws;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CalculatorTest {

    @BeforeClass
    public static void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("openejb.embedded.remotable", "true");
        properties.setProperty("httpejbd.print", "true");
        properties.setProperty("httpejbd.indent.xml", "true");
        EJBContainer.createEJBContainer(properties);
    }

    @Test
    public void test() throws Exception {
        Service calculatorService = Service.create(
                new URL("http://127.0.0.1:4204/simple-webservice/Calculator?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorService"));

        assertNotNull(calculatorService);

        CalculatorWs calculator = calculatorService.getPort(CalculatorWs.class);
        assertEquals(10, calculator.sum(4, 6));
        assertEquals(12, calculator.multiply(3, 4));
    }
}
