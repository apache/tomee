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
package org.superbiz.calculator;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Properties;

public class CalculatorTest extends TestCase {

	//START SNIPPET: setup	
	private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");
        
        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup    

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    public void testCalculatorViaWsInterface() throws Exception {
        Service calcService = Service.create(new URL("http://localhost:4204/CalculatorImpl?wsdl"), null);
        assertNotNull(calcService);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);
        assertEquals(10, calc.sum(4,6));
		assertEquals(12, calc.multiply(3,4));
    }
    //END SNIPPET: webservice

}
