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

package ee.jakarta.tck.ws.rs.api.rs.core.cookie;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Cookie;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = 724842945840527973L;

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
     * @assertion_ids: JAXRS:JAVADOC:51; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a Cookie instance using Constructor Cookie(String,
     * String)
     */
    @Test
    public void constructorTest1() throws Fault {
        String name = "name_1";
        String value = "value_1";
        int version = 1;
        String domain = "";
        String path = "";

        Cookie ck1 = new Cookie(name, value);
        verifyCookie(ck1, name, value, path, domain, version);
    }

    /*
     * @testName: constructorTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a Cookie instance using Constructor Cookie(String,
     * String, String, String)
     */
    @Test
    public void constructorTest2() throws Fault {
        String name = "name_1";
        String value = "value_1";
        String path = "/acme";
        String domain = "";
        int version = 1;

        Cookie ck2 = new Cookie(name, value, path, domain);

        verifyCookie(ck2, name, value, path, domain, version);
    }

    /*
     * @testName: constructorTest3
     * 
     * @assertion_ids: JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a Cookie instance using Constructor Cookie(String,
     * String, String, String)
     */
    @Test
    public void constructorTest3() throws Fault {
        String name = "name_1";
        String value = "value_1";
        String path = "";
        String domain = "y.x.foo.com";
        int version = 1;

        Cookie ck3 = new Cookie(name, value, path, domain);
        verifyCookie(ck3, name, value, path, domain, version);
    }

    /*
     * @testName: constructorTest4
     * 
     * @assertion_ids: JAXRS:JAVADOC:51; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a version 1 Cookie instance using Constructor
     * Cookie(String, String, String, String, int)
     */
    @Test
    public void constructorTest4() throws Fault {
        String name = "name_1";
        String value = "value_1";
        String path = "/acme";
        String domain = "y.x.foo.com";
        int version = 1;

        Cookie ck4 = new Cookie(name, value, path, domain, version);
        verifyCookie(ck4, name, value, path, domain, version);
    }

    /*
     * @testName: constructorTest5
     * 
     * @assertion_ids: JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a version 0 Cookie instance using Constructor
     * Cookie(String, String, String, String, int)
     */
    @Test
    public void constructorTest5() throws Fault {
        String name = "name_1";
        String value = "value_1";
        String path = "/acme";
        String domain = "y.x.foo.com";
        int version = 0;

        Cookie ck5 = new Cookie(name, value, path, domain, version);
        verifyCookie(ck5, name, value, path, domain, version);
    }

    /*
     * @testName: parseTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:60; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a version 0 Cookie instance by Parsing a String
     */
    @Test
    public void parseTest1() throws Fault {
        String cookie_toParse = "NAME_1=Value_1;";
        String name = "name_1";
        String value = "value_1";
        String path = "";
        String domain = "";
        int version = 0;

        Cookie ck6 = jakarta.ws.rs.core.Cookie.valueOf(cookie_toParse);
        verifyCookie(ck6, name, value, path, domain, version);
    }

    /*
     * @testName: parseTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:60; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
     * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56; JAXRS:JAVADOC:57;
     * 
     * @test_Strategy: Create a version 0 Cookie instance by Parsing a String
     */
    @Test
    public void parseTest2() throws Fault {
        String cookie_toParse = "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"";

        String name = "customer";
        String value = "wile_e_coyote";
        String path = "/acme";
        String domain = "";
        int version = 1;

        Cookie ck7 = jakarta.ws.rs.core.Cookie.valueOf(cookie_toParse);

        verifyCookie(ck7, name, value, path, domain, version);
    }

    /*
     * @testName: parseTest3
     * 
     * @assertion_ids: JAXRS:JAVADOC:60;
     * 
     * @test_Strategy: Testing Correcting exception thrown when calling
     * Cookie.valueOf(null)
     */
    @Test
    public void parseTest3() throws Fault {
        try {
            jakarta.ws.rs.core.Cookie.valueOf(null);
            throw new Fault("Expectecd IllegalArgumentException not thrown.  Test Failed");
        } catch (java.lang.IllegalArgumentException ilex) {
            logMsg("IllegalArgumentException has been thrown as expected");
        }
    }

    /*
     * @testName: equalsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:49; JAXRS:JAVADOC:52; JAXRS:JAVADOC:58;
     * 
     * @test_Strategy: Create two Cookie instances using Constructor Cookie(String,
     * String, String, String, int). Change the parameters one by one, make sure
     * hashCode() and equals method work.
     */
    @Test
    public void equalsTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        String name = "name_1";
        String value = "value_1";
        String path = "/acme";
        String domain = "y.x.foo.com";
        int version = 0;

        Cookie ck9 = new Cookie(name, value, path, domain, version);
        Cookie ck10 = new Cookie(name, value, path, domain, version);

        if (ck9.hashCode() != ck10.hashCode()) {
            pass = false;
            sb.append("First hashCode test failed" + newline);
        }

        if (!ck9.equals(ck10)) {
            pass = false;
            sb.append("First Equal test failed" + newline);
        }

        version = 1;
        ck10 = new Cookie(name, value, path, domain, version);
        if (ck9.hashCode() == ck10.hashCode()) {
            pass = false;
            sb.append("hashCode test failed at version" + newline);
        }

        if (ck9.equals(ck10)) {
            pass = false;
            sb.append("UnEqual test failed at version" + newline);
        }

        name = "name_2";
        ck9 = new Cookie(name, value, path, domain, version);
        if (ck9.hashCode() == ck10.hashCode()) {
            pass = false;
            sb.append("hashCode test failed at name" + newline);
        }

        if (ck9.equals(ck10)) {
            pass = false;
            sb.append("UnEqual test failed at name" + newline);
        }

        value = "value_2";
        ck10 = new Cookie(name, value, path, domain, version);
        if (ck9.hashCode() == ck10.hashCode()) {
            pass = false;
            sb.append("hashCode test failed at value" + newline);
        }

        if (ck9.equals(ck10)) {
            pass = false;
            sb.append("UnEqual test failed at value" + newline);
        }

        path = "/test";
        ck9 = new Cookie(name, value, path, domain, version);
        if (ck9.hashCode() == ck10.hashCode()) {
            pass = false;
            sb.append("hashCode test failed at path" + newline);
        }

        if (ck9.equals(ck10)) {
            pass = false;
            sb.append("UnEqual test failed at path" + newline);
        }

        domain = "sun.com";
        ck9 = new Cookie(name, value, path, domain, version);
        if (ck9.hashCode() == ck10.hashCode()) {
            pass = false;
            sb.append("hashCode test failed at domain" + newline);
        }

        if (ck9.equals(ck10)) {
            pass = false;
            sb.append("UnEqual test failed at domain" + newline);
        }

        assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }

    /*
     * @testName: toStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:49; JAXRS:JAVADOC:59;
     * 
     * @test_Strategy: Create two Cookie instances using Constructor Cookie(String,
     * String, String, String, int). Change the parameters one by one, make sure
     * hashCode() and equals method work.
     */
    @Test
    public void toStringTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        String name = "name_1";
        String value = "value_1";
        String path = "/acme";
        String domain = "y.x.foo.com";
        int version = 0;

        Cookie ck11 = new Cookie(name, value, path, domain, version);

        String cookie = ck11.toString().toLowerCase();
        if (!cookie.contains("name_1")) {
            pass = false;
            sb.append("Name test failed" + newline);
        }

        if (!cookie.contains("value_1")) {
            pass = false;
            sb.append("Value test failed" + newline);
        }

        if (!cookie.contains("acme")) {
            pass = false;
            sb.append("path test failed" + newline);
        }

        if (!cookie.contains("y.x.foo.com")) {
            pass = false;
            sb.append("domain test failed" + newline);
        }

        assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }

    private static boolean verifyCookie(Cookie ck, String name, String value, String path, String domain, int version)
            throws Fault {

        StringBuffer sb = new StringBuffer();
        boolean pass = true;

        if (name == "" || name == null) {
            pass = false;
            sb.append("Cookie's name is empty");
        } else if (!ck.getName().toLowerCase().equals(name)) {
            pass = false;
            sb.append("Failed name test.  Expect " + name + " got " + ck.getName());
        }

        if (value == "" || value == null) {
            pass = false;
            sb.append("Cookie's value is empty");
        } else if (!ck.getValue().toLowerCase().equals(value)) {
            pass = false;
            sb.append("Failed value test.  Expect " + value + " got " + ck.getValue());
        }

        if (ck.getVersion() != version) {
            pass = false;
            sb.append("Failed version test.  Expect " + version + " got " + ck.getVersion());
        }

        if (path == "" || path == null) {
            if (ck.getPath() != "" && ck.getPath() != null) {
                pass = false;
                sb.append("Failed path test.  Expect null String, got " + ck.getPath());
            }
        } else if (ck.getPath() == null || ck.getPath() == "") {
            pass = false;
            sb.append("Failed path test.  Got null, expecting " + path);
        } else if (!ck.getPath().toLowerCase().equals(path)) {
            pass = false;
            sb.append("Failed path test.  Expect " + path + " got " + ck.getPath());
        }

        if (domain == "" || domain == null) {
            if (ck.getDomain() != "" && ck.getDomain() != null) {
                pass = false;
                sb.append("Failed path test.  Expect " + domain + " got " + ck.getDomain());
            }
        } else if (!ck.getDomain().toLowerCase().equals(domain)) {
            pass = false;
            sb.append("Failed domain test.  Expect " + domain + " got " + ck.getDomain());
        }

        assertTrue(pass, "At least one assertion falied: " + sb.toString());

        return pass;
    }
}
