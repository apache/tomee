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
package org.superbiz.ws.out;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CalculatorTest {

    //Random port to avoid test conflicts
    private static final int port = Integer.parseInt(System.getProperty("httpejbd.port", "" + org.apache.openejb.util.NetworkUtil.getNextAvailablePort()));

    @BeforeClass
    public static void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("openejb.embedded.remotable", "true");

        //Just for this test we change the default port from 4204 to avoid conflicts
        properties.setProperty("httpejbd.port", "" + port);

        // properties.setProperty("httpejbd.print", "true");
        // properties.setProperty("httpejbd.indent.xml", "true");
        EJBContainer.createEJBContainer(properties);
    }

    @Test
    public void outParams() throws Exception {
        final Service calculatorService = Service.create(
                new URL("http://localhost:" + port + "/webservice-holder/Calculator?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorService"));

        assertNotNull(calculatorService);

        final CalculatorWs calculator = calculatorService.getPort(CalculatorWs.class);

        final Holder<Integer> sum = new Holder<Integer>();
        final Holder<Integer> multiply = new Holder<Integer>();

        calculator.sumAndMultiply(4, 6, sum, multiply);

        assertEquals(10, (int) sum.value);
        assertEquals(24, (int) multiply.value);
    }
}
