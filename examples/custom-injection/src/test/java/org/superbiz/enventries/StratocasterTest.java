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
package org.superbiz.enventries;

import static java.util.Arrays.asList;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.Context;

import java.util.Locale;
import java.util.Properties;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.InetAddress;
import java.text.DateFormat;

/**
 * @version $Rev$ $Date$
 */
//START SNIPPET: code
public class StratocasterTest extends TestCase {

    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

        initialContext = new InitialContext(properties);
    }

    public void test() throws Exception {
        Stratocaster strat = (Stratocaster) initialContext.lookup("StratocasterImplLocal");


        Date date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).parse("Mar 1, 1962");
        assertEquals("Strat.getDateCreated()", date, strat.getDateCreated());

        List<Pickup> pickups = asList(Pickup.SINGLE_COIL, Pickup.SINGLE_COIL, Pickup.SINGLE_COIL);
        assertEquals("Strat.getPickups()", pickups, strat.getPickups());

        assertEquals("Strat.getStyle()", Style.VINTAGE, strat.getStyle());

        assertEquals("Strat.getStringGuage(\"E1\")", 0.052F, strat.getStringGuage("E1"));
        assertEquals("Strat.getStringGuage(\"A\")", 0.042F, strat.getStringGuage("A"));
        assertEquals("Strat.getStringGuage(\"D\")", 0.030F, strat.getStringGuage("D"));
        assertEquals("Strat.getStringGuage(\"G\")", 0.017F, strat.getStringGuage("G"));
        assertEquals("Strat.getStringGuage(\"B\")", 0.013F, strat.getStringGuage("B"));
        assertEquals("Strat.getStringGuage(\"E\")", 0.010F, strat.getStringGuage("E"));

        File file = new File("/tmp/strat-certificate.txt");
        assertEquals("Strat.getCertificateOfAuthenticity()", file, strat.getCertificateOfAuthenticity());


    }
}
//END SNIPPET: code
