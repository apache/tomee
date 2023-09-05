/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.api.rs.core.entitytag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.EntityTag;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = -5886257224223478590L;

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /*
     * @testName: constructorTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:62; JAXRS:JAVADOC:65; JAXRS:JAVADOC:67;
     * 
     * @test_Strategy: Create an EntityTag instance using entityTag(String)
     */
    @Test
    public void constructorTest1() throws Fault {
        String value = "cts test entity tag test";
        boolean strong = true;

        EntityTag et1 = new EntityTag(value);
        verifyEntityTag(et1, value, strong);
    }

    /*
     * @testName: constructorTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:63; JAXRS:JAVADOC:65; JAXRS:JAVADOC:67;
     * 
     * @test_Strategy: Create an EntityTag instance using entityTag(String, boolean)
     */
    @Test
    public void constructorTest2() throws Fault {
        String value = "cts test entity tag test weak";
        boolean strong = false;

        EntityTag et2 = new EntityTag(value, true);

        verifyEntityTag(et2, value, strong);

        strong = true;

        EntityTag et3 = new EntityTag(value, false);

        verifyEntityTag(et3, value, strong);
    }

    /*
     * @testName: equalsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:62; JAXRS:JAVADOC:63; JAXRS:JAVADOC:64;
     * JAXRS:JAVADOC:66;
     * 
     * @test_Strategy: Create two EntityTag instances using either entityTag(String,
     * boolean) and entityTag(String). Verify EntityTag.equals(Object) and
     * hashCode() work
     */
    @Test
    public void equalsTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        String value = "cts test entity tag test";

        EntityTag et4 = new EntityTag(value);
        EntityTag et5 = new EntityTag(value, false);

        if (!et4.equals(et5)) {
            pass = false;
            sb.append("Strong entity Tag equals test failed. " + et4.toString() + ";" + et5.toString() + ";");
        }

        if (et4.hashCode() != et5.hashCode()) {
            pass = false;
            sb.append("Strong entity Tag hashCode test failed. " + et4.toString() + ";" + et5.toString() + ";");
        }

        value = "cts test entity tag test weak";
        EntityTag et6 = new EntityTag(value, true);
        EntityTag et7 = new EntityTag(value, false);

        if (et6.equals(et7)) {
            pass = false;
            sb.append("Weak entity Tag equals test failed. " + et6.toString() + ";" + et7.toString() + ";");
        }

        if (et6.hashCode() == et7.hashCode()) {
            pass = false;
            sb.append("Weak entity Tag hashCode test failed. " + et6.toString() + ";" + et7.toString() + ";");
        }

        assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }

    /*
     * @testName: toStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:62; JAXRS:JAVADOC:63; JAXRS:JAVADOC:68;
     * 
     * @test_Strategy: Create two EntityTag instances using either entityTag(String,
     * boolean) and entityTag(String). Verify EntityTag.toString() works
     */
    @Test
    public void toStringTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        String value = "cts test Strong EntityTag test";

        EntityTag et8 = new EntityTag(value);
        String header = et8.toString();
        sb.append(header + "." + newline);

        if (!header.contains(value)) {
            pass = false;
            sb.append("Strong EnttyTag ToString Test failed: " + header + " does not contain " + value);
        }

        value = "cts test Weak EntityTag test";

        EntityTag et9 = new EntityTag(value, true);

        header = et9.toString();
        sb.append(header + "." + newline);

        if (!header.contains(value)) {
            pass = false;
            sb.append("Weak EnttyTag ToString Test failed: " + header + " does not contain " + value);
        }

        assertTrue(pass, "At least one assertion failed: " + sb.toString());
        System.out.println(sb.toString());
    }

    /*
     * @testName: valueOfTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:69;
     * 
     * @test_Strategy: Create an EntityTag instances using EntityTag.valueOf(null)
     * Verify IllegalArgumentException thrown
     */
    @Test
    public void valueOfTest() throws Fault {
        try {
            EntityTag.valueOf(null);
            throw new Fault("Expected IllegalArgumentException not thrown.  Test failed.");
        } catch (java.lang.IllegalArgumentException ilex) {
            logMsg("IllegalArgumentException has been thrown as expected");
        }
    }

    /*
     * @testName: valueOfTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:69;
     * 
     * @test_Strategy: EntityTag.valueOf(value) does not throw Exception
     */
    @Test
    public void valueOfTest1() throws Fault {
        String value = "\"cts test Strong EntityTag test\"";
        try {
            EntityTag et10 = EntityTag.valueOf(value);
            verifyEntityTag(et10, value, true);
        } catch (Exception ilex) {
            throw new Fault("Unexpected exception throw: " + ilex.getMessage());
        }
    }

    private static void verifyEntityTag(EntityTag et, String value, boolean strong) throws Fault {
        StringBuffer sb = new StringBuffer();
        boolean pass = true;

        if (!et.getValue().toLowerCase().replace("\"", "").equals(value.replace("\"", "").toLowerCase())) {
            pass = false;
            sb.append("Failed value test.  Expect " + value + " got " + et.getValue() + ".");
        }

        if (et.isWeak() == strong) {
            pass = false;
            sb.append("Failed isWeak test.  Expect " + !strong + " got " + et.isWeak());
        }

        assertTrue(pass, "at least one assertion failed: " + sb.toString());
    }
}
