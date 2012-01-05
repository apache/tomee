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
import org.apache.openejb.api.LocalClient;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

@LocalClient
public class CalculatorTest extends TestCase {

    @WebServiceRef(
            wsdlLocation = "http://127.0.0.1:4204/CalculatorImpl?wsdl"
    )
    private CalculatorWs calculatorWs;

    //START SNIPPET: setup	
    private InitialContext initialContext;

    // date used to invoke a web service with INOUT parameters
    private static final Date date = new Date();

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        initialContext = new InitialContext(properties);
        initialContext.bind("inject", this);

    }
    //END SNIPPET: setup    

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    public void testCalculatorViaWsInterface() throws Exception {
        Service calcService = Service.create(
                new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);
        assertEquals(10, calc.sum(4, 6));
        assertEquals(12, calc.multiply(3, 4));

        Holder<String> userIdHolder = new Holder<String>("jane");
        Holder<String> returnCodeHolder = new Holder<String>();
        Holder<Date> datetimeHolder = new Holder<Date>(date);
        assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
        assertEquals(userIdHolder.value, returnCodeHolder.value);
        assertTrue(date.before(datetimeHolder.value));
    }

    public void testWebServiceRefInjection() throws Exception {
        assertEquals(10, calculatorWs.sum(4, 6));
        assertEquals(12, calculatorWs.multiply(3, 4));

        Holder<String> userIdHolder = new Holder<String>("jane");
        Holder<String> returnCodeHolder = new Holder<String>();
        Holder<Date> datetimeHolder = new Holder<Date>(date);
        assertEquals(6, calculatorWs.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
        assertEquals(userIdHolder.value, returnCodeHolder.value);
        assertTrue(date.before(datetimeHolder.value));
    }

    public void testCalculatorViaRemoteInterface() throws Exception {
        CalculatorLocal calc = (CalculatorLocal) initialContext.lookup("CalculatorImplLocal");
        assertEquals(10, calc.sum(4, 6));
        assertEquals(12, calc.multiply(3, 4));

        Holder<String> userIdHolder = new Holder<String>("jane");
        Holder<String> returnCodeHolder = new Holder<String>();
        Holder<Date> datetimeHolder = new Holder<Date>(date);
        assertEquals(6, calc.factorial(3, userIdHolder, returnCodeHolder, datetimeHolder));
        assertEquals(userIdHolder.value, returnCodeHolder.value);
        assertTrue(date.before(datetimeHolder.value));
    }
    //END SNIPPET: webservice

}
