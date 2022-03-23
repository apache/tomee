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
package org.superbiz.calculator;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    private static EJBContainer container;

    //Random port to avoid test conflicts
    private static final int port = Integer.parseInt(System.getProperty("httpejbd.port", "" + org.apache.openejb.util.NetworkUtil.getNextAvailablePort()));

    @BeforeClass
    public static void setUp() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("openejb.embedded.remotable", "true");

        //Just for this test we change the default port from 4204 to avoid conflicts
        properties.setProperty("httpejbd.port", "" + port);

        container = EJBContainer.createEJBContainer(properties);
    }

    @Before
    public void inject() throws NamingException {
        if (container != null) {
            container.getContext().bind("inject", this);
        }
    }

    @AfterClass
    public static void close() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void wsdlExists() throws Exception {
        final URL url = new URL("http://localhost:" + port + "/simple-webservice-without-interface/Calculator?wsdl");
        assertTrue(IOUtils.readLines(url.openStream()).size() > 0);
        assertTrue(IOUtils.readLines(url.openStream()).toString().contains("CalculatorWsService"));
    }
}
