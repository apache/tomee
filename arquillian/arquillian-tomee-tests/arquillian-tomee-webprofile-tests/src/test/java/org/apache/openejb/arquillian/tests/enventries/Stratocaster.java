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
package org.apache.openejb.arquillian.tests.ext.enventries;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Rev$ $Date$
 */
public class Stratocaster extends HttpServlet {


    @Resource(name = "pickups")
    private List<Pickup> pickups;

    @Resource(name = "style")
    private Style style;

    @Resource(name = "dateCreated")
    private Date dateCreated;

    @Resource(name = "guitarStringGuages")
    private Map<String, Float> guitarStringGuages;

    @Resource(name = "certificateOfAuthenticity")
    private File certificateOfAuthenticity;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();

        try {

            try {
                final Date date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).parse("Mar 1, 1962");
                assertEquals("Strat.getDateCreated()", date, this.getDateCreated());
            } catch (ParseException e) {
                fail(e.getMessage());
            }

            List<Pickup> pickups = asList(Pickup.SINGLE_COIL, Pickup.SINGLE_COIL, Pickup.SINGLE_COIL);
            assertEquals("Strat.getPickups()", pickups, this.getPickups());

            assertEquals("Strat.getStyle()", Style.VINTAGE, this.getStyle());

            assertEquals("Strat.getStringGuage(\"E1\")", 0.052F, this.getStringGuage("E1"), 0.);
            assertEquals("Strat.getStringGuage(\"A\")", 0.042F, this.getStringGuage("A"), 0.);
            assertEquals("Strat.getStringGuage(\"D\")", 0.030F, this.getStringGuage("D"), 0.);
            assertEquals("Strat.getStringGuage(\"G\")", 0.017F, this.getStringGuage("G"), 0.);
            assertEquals("Strat.getStringGuage(\"B\")", 0.013F, this.getStringGuage("B"), 0.);
            assertEquals("Strat.getStringGuage(\"E\")", 0.010F, this.getStringGuage("E"), 0.);

            File file = new File("/tmp/strat-certificate.txt");
            assertEquals("Strat.getCertificateOfAuthenticity()", file, this.getCertificateOfAuthenticity());

            writer.write("[passed]");
        } catch (Throwable e) {
            writer.println("false");
            writer.println("");
            writer.println("STACKTRACE");
            writer.println("");
            e.printStackTrace(writer);
        }
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Gets the guage of the electric guitar strings used in this guitar.
     *
     * @param string
     * @return
     */
    public float getStringGuage(String string) {
        return guitarStringGuages.get(string);
    }

    public List<Pickup> getPickups() {
        return pickups;
    }

    public Style getStyle() {
        return style;
    }

    public File getCertificateOfAuthenticity() {
        return certificateOfAuthenticity;
    }
}
