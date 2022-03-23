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
package org.superbiz.enventries;

import junit.framework.TestCase;

import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
//START SNIPPET: code
public class StratocasterTest extends TestCase {

    @EJB
    private Stratocaster strat;

    public void test() throws Exception {
        EJBContainer.createEJBContainer().getContext().bind("inject", this);

        Date date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).parse("Mar 1, 1962");
        assertEquals("Strat.getDateCreated()", date, strat.getDateCreated());

        List<Pickup> pickups = asList(Pickup.SINGLE_COIL, Pickup.SINGLE_COIL, Pickup.SINGLE_COIL);
        assertEquals("Strat.getPickups()", pickups, strat.getPickups());

        assertEquals("Strat.getStyle()", Style.VINTAGE, strat.getStyle());

        assertEquals("Strat.getStringGuage(\"E1\")", 0.052F, strat.getStringGuage("E1"), 1e-15);
        assertEquals("Strat.getStringGuage(\"A\")", 0.042F, strat.getStringGuage("A"), 1e-15);
        assertEquals("Strat.getStringGuage(\"D\")", 0.030F, strat.getStringGuage("D"), 1e-15);
        assertEquals("Strat.getStringGuage(\"G\")", 0.017F, strat.getStringGuage("G"), 1e-15);
        assertEquals("Strat.getStringGuage(\"B\")", 0.013F, strat.getStringGuage("B"), 1e-15);
        assertEquals("Strat.getStringGuage(\"E\")", 0.010F, strat.getStringGuage("E"), 1e-15);

        File file = new File("/tmp/strat-certificate.txt");
        assertEquals("Strat.getCertificateOfAuthenticity()", file, strat.getCertificateOfAuthenticity());

    }
}
//END SNIPPET: code
