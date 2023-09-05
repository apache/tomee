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

package ee.jakarta.tck.ws.rs.api.rs.core.cachecontrol;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.CacheControl;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {
    private static final long serialVersionUID = 1L;

    static Object[] expected = { (boolean) true, (boolean) false, (boolean) false, (boolean) false, (boolean) true,
            (boolean) false, (boolean) false, new ArrayList<String>(), new ArrayList<String>() };

    static String _root = "/jaxrs_rs_core_cacheControlTest_web";

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /*
     * @testName: emptyConstructorTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void emptyConstructorTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        CacheControl ccl = new CacheControl();
        pass = verifyList(expected, ccl, sb);
        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: maxAgeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:30; JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34;
     * JAXRS:JAVADOC:35; JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:38;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void maxAgeTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        int maxAge = -1;

        CacheControl ccl1 = new CacheControl();
        ccl1.setMaxAge(maxAge);

        pass = verifyList(expected, ccl1, sb);
        pass &= verifyMaxAge(ccl1, maxAge, sb);

        maxAge = 2000;
        ccl1.setMaxAge(maxAge);
        pass &= verifyList(expected, ccl1, sb);
        pass &= verifyMaxAge(ccl1, maxAge, sb);

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: sMaxAgeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:27; JAXRS:JAVADOC:28;
     * JAXRS:JAVADOC:29; JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34;
     * JAXRS:JAVADOC:35; JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:45;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void sMaxAgeTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        int sMaxAge = -1;

        CacheControl ccl1 = new CacheControl();
        ccl1.setSMaxAge(sMaxAge);

        pass = verifyList(expected, ccl1, sb);
        pass &= verifySMaxAge(ccl1, sMaxAge, sb);

        sMaxAge = 2000;
        ccl1.setSMaxAge(sMaxAge);
        pass &= verifyList(expected, ccl1, sb);
        pass &= verifySMaxAge(ccl1, sMaxAge, sb);

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: revalidateTest
     *
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:39;
     *
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void revalidateTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean revalidate = false;
        expected[5] = revalidate;

        CacheControl ccl2 = new CacheControl();
        ccl2.setMustRevalidate(revalidate);

        pass = verifyList(expected, ccl2, sb);
        sb.append("Finished the first round");

        revalidate = true;
        expected[5] = revalidate;

        ccl2.setMustRevalidate(revalidate);

        pass &= verifyList(expected, ccl2, sb);

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: noCacheTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:40;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void noCacheTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean nocache = false;
        expected[2] = nocache;

        CacheControl ccl3 = new CacheControl();
        ccl3.setNoCache(nocache);

        pass = verifyList(expected, ccl3, sb);

        nocache = true;
        expected[2] = nocache;

        ccl3.setNoCache(nocache);
        pass &= verifyList(expected, ccl3, sb);

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: noStoreTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:41;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void noStoreTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean nostore = false;
        expected[3] = nostore;

        CacheControl ccl4 = new CacheControl();
        ccl4.setNoStore(nostore);

        pass = verifyList(expected, ccl4, sb);

        nostore = true;
        expected[3] = nostore;

        ccl4.setNoStore(nostore);

        pass &= verifyList(expected, ccl4, sb);
        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: noTransformTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:42;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void noTransformTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean notransform = false;
        expected[4] = notransform;

        CacheControl ccl4 = new CacheControl();
        ccl4.setNoTransform(notransform);

        pass = verifyList(expected, ccl4, sb);

        notransform = true;
        expected[4] = notransform;

        ccl4.setNoTransform(notransform);

        pass &= verifyList(expected, ccl4, sb);
        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: privateTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:43;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void privateTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean _private = false;
        expected[1] = _private;

        CacheControl ccl5 = new CacheControl();
        ccl5.setPrivate(_private);

        pass = verifyList(expected, ccl5, sb);

        _private = true;
        expected[1] = _private;

        ccl5.setPrivate(_private);

        pass &= verifyList(expected, ccl5, sb);
        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: proxyRevalidateTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:28; JAXRS:JAVADOC:29;
     * JAXRS:JAVADOC:32; JAXRS:JAVADOC:33; JAXRS:JAVADOC:34; JAXRS:JAVADOC:35;
     * JAXRS:JAVADOC:36; JAXRS:JAVADOC:37; JAXRS:JAVADOC:44;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Verify all values are set correctly.
     */
    @Test
    public void proxyRevalidateTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        boolean proxyRevalidate = false;
        expected[6] = proxyRevalidate;

        CacheControl ccl5 = new CacheControl();
        ccl5.setProxyRevalidate(proxyRevalidate);

        pass = verifyList(expected, ccl5, sb);

        proxyRevalidate = true;
        expected[6] = proxyRevalidate;

        ccl5.setProxyRevalidate(proxyRevalidate);

        pass &= verifyList(expected, ccl5, sb);
        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: equalsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:25; JAXRS:JAVADOC:31;
     * JAXRS:JAVADOC:38; JAXRS:JAVADOC:39; JAXRS:JAVADOC:40; JAXRS:JAVADOC:41;
     * JAXRS:JAVADOC:42; JAXRS:JAVADOC:43; JAXRS:JAVADOC:44; JAXRS:JAVADOC:45;
     * 
     * @test_Strategy: Client instantiate two CacheControl instance using
     * Constructor CacheControl(). Setting all their properties one by one, verify
     * hashCode and equals methods work correctly and accordingly.
     */
    @Test
    public void equalsTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        CacheControl ccl6 = new CacheControl();
        CacheControl ccl7 = new CacheControl();

        ccl6.setProxyRevalidate(false);
        ccl7.setProxyRevalidate(true);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setProxyRevalidate failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setProxyRevalidate failed." + newline);
        }

        ccl7.setProxyRevalidate(false);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setProxyRevalidate failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setProxyRevalidate failed." + newline);
        }

        ccl6.setPrivate(true);
        ccl7.setPrivate(false);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setPrivate failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setPrivate failed." + newline);
        }

        ccl7.setPrivate(true);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setPrivate failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setPrivate failed." + newline);
        }

        ccl6.setNoTransform(true);
        ccl7.setNoTransform(false);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setNoTransform failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setNoTransform failed." + newline);
        }

        ccl7.setNoTransform(true);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setNoTransform failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setNoTransform failed." + newline);
        }

        ccl6.setNoStore(true);
        ccl7.setNoStore(false);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setNoStore failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setNoStore failed." + newline);
        }

        ccl7.setNoStore(true);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setNoStore failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setNoStore failed." + newline);
        }

        ccl6.setNoCache(true);
        ccl7.setNoCache(false);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setNoCache failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setNoCache failed." + newline);
        }

        ccl7.setNoCache(true);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setNoCache failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setNoCache failed." + newline);
        }

        ccl6.setMustRevalidate(true);
        ccl7.setMustRevalidate(false);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setMustRevalidate failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setMustRevalidate failed." + newline);
        }

        ccl7.setMustRevalidate(true);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setMustRevalidate failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode Equal Test setMustRevalidate failed." + newline);
        }

        ccl6.setSMaxAge(-1);
        ccl7.setSMaxAge(200);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setSMaxAge failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setSMaxAge failed." + newline);
        }

        ccl7.setSMaxAge(-1);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setSMaxAge failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setSMaxAge failed." + newline);
        }

        ccl6.setMaxAge(-1);
        ccl7.setMaxAge(200);
        if (ccl6.equals(ccl7)) {
            pass = false;
            sb.append("UnEqual Test setMaxAge failed." + newline);
        }

        if (ccl6.hashCode() == ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setMaxAge failed." + newline);
        }

        ccl7.setMaxAge(-1);
        if (!ccl6.equals(ccl7)) {
            pass = false;
            sb.append("Equal Test setMaxAge failed." + newline);
        }

        if (ccl6.hashCode() != ccl7.hashCode()) {
            pass = false;
            sb.append("HashCode UnEqual Test setMaxAge failed." + newline);
        }

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: toStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:24; JAXRS:JAVADOC:38; JAXRS:JAVADOC:39;
     * JAXRS:JAVADOC:40; JAXRS:JAVADOC:41; JAXRS:JAVADOC:42; JAXRS:JAVADOC:43;
     * JAXRS:JAVADOC:44; JAXRS:JAVADOC:45; JAXRS:JAVADOC:46;
     * 
     * @test_Strategy: Client instantiate a CacheControl instance using Constructor
     * CacheControl(). Setting all their properties verify toString method work
     * correctly and accordingly.
     */
    @Test
    public void toStringTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        CacheControl ccl8 = new CacheControl();

        ccl8.setProxyRevalidate(true);
        ccl8.setPrivate(true);
        ccl8.setNoTransform(true);
        ccl8.setNoStore(true);
        ccl8.setNoCache(true);
        ccl8.setMustRevalidate(false);
        ccl8.setSMaxAge(-1);
        ccl8.setMaxAge(200);

        String value = ccl8.toString().toLowerCase();
        sb.append(value + newline);

        if (!value.contains("private")) {
            pass = false;
            sb.append("ToString test failed in Private" + newline);
        }

        if (!value.contains("no-cache")) {
            pass = false;
            sb.append("ToString test failed in no-cache" + newline);
        }

        if (!value.contains("no-store")) {
            pass = false;
            sb.append("ToString test failed in no-store" + newline);
        }

        if (!value.contains("no-transform")) {
            pass = false;
            sb.append("ToString test failed in no-transform" + newline);
        }

        if (!value.contains("proxy-revalidate")) {
            pass = false;
            sb.append("ToString test failed in proxy-revalidate" + newline);
        }

        if (!value.contains("max-age=200")) {
            pass = false;
            sb.append("ToString test failed in max-age=200" + newline);
        }

        if (value.contains("s-maxage")) {
            pass = false;
            sb.append("ToString test failed in s-maxage" + newline);
        }

        if (value.contains("must-revalidate")) {
            pass = false;
            sb.append("ToString test failed in must-revalidate" + newline);
        }

        assertTrue(pass, "At least one assertion failed: " + sb);
    }

    /*
     * @testName: getExtensionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:26;
     * 
     * @test_Strategy: Client instantiate one CacheControl instance using
     * Constructor CacheControl(). verify getCacheExtension method work correctly.
     */
    @Test
    public void getExtensionTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        Map<String, String> map_exp = new HashMap<String, String>();
        map_exp.put("community", "\"UCI\"");

        CacheControl ccl8 = new CacheControl();

        ccl8.setProxyRevalidate(true);
        ccl8.setPrivate(true);
        ccl8.setNoTransform(true);
        ccl8.setNoStore(true);
        ccl8.setNoCache(true);
        ccl8.setMustRevalidate(false);
        ccl8.setSMaxAge(-1);
        ccl8.setMaxAge(200);

        Map<String, String> map_actual = ccl8.getCacheExtension();

        if (map_actual.size() != 0) {
            pass = false;
            sb.append("Expected map size 0, got" + map_actual.size() + newline);
        }

        map_actual.put("community", "\"UCI\"");
        map_actual = ccl8.getCacheExtension();

        if (!map_exp.equals(map_actual)) {
            pass = false;
            sb.append("Map comparison failed" + newline);
        }

        sb.append("Expected Map: ");

        for (Entry<String, String> entry : map_exp.entrySet()) {
            sb.append("key=" + entry.getKey() + "; value=" + entry.getValue() + ";" + newline);
        }

        if (!pass) {
            for (Entry<String, String> entry : map_actual.entrySet()) {
                sb.append("key= " + entry.getKey() + "; value=" + entry.getValue() + ";");
                sb.append(newline);
            }
            throw new Fault("At least one assertion failed: " + sb.toString());
        }

        TestUtil.logTrace(sb.toString());
    }

    /*
     * @testName: valueOfTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:47;
     * 
     * @test_Strategy: Client instantiate one CacheControl instance using
     * Constructor CacheControl.valueOf(String). verify valueOf method work
     * correctly.
     */
    @Test
    public void valueOfTest() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        String value_to_parse = "private, no-cache, no-store, no-transform, proxy-revalidate, max-age=200";

        CacheControl ccl8 = CacheControl.valueOf(value_to_parse);

        String value = ccl8.toString().toLowerCase();
        sb.append(value + newline);

        if (!value.contains("private")) {
            pass = false;
            sb.append("ToString test failed in Private" + newline);
        }

        if (!value.contains("no-cache")) {
            pass = false;
            sb.append("ToString test failed in no-cache" + newline);
        }

        if (!value.contains("no-store")) {
            pass = false;
            sb.append("ToString test failed in no-store" + newline);
        }

        if (!value.contains("no-transform")) {
            pass = false;
            sb.append("ToString test failed in no-transform" + newline);
        }

        if (!value.contains("proxy-revalidate")) {
            pass = false;
            sb.append("ToString test failed in proxy-revalidate" + newline);
        }

        if (!value.contains("max-age=200")) {
            pass = false;
            sb.append("ToString test failed in max-age=200" + newline);
        }

        if (value.contains("s-maxage")) {
            pass = false;
            sb.append("ToString test failed in s-maxage" + newline);
        }

        if (value.contains("must-revalidate")) {
            pass = false;
            sb.append("ToString test failed in must-revalidate" + newline);
        }

        assertTrue(pass, "At least one assertion failed: " + sb);

        TestUtil.logTrace("Test passed. " + sb.toString());
    }

    /*
     * @testName: valueOfTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:47;
     * 
     * @test_Strategy: Client instantiate one CacheControl instance using
     * Constructor CacheControl.valueOf(null). verify IllegalArgumentException is
     * thrown.
     */
    @Test
    public void valueOfTest1() throws Fault {
        boolean pass = true;
        StringBuffer sb = new StringBuffer();

        try {
            CacheControl.valueOf(null);
            pass = false;
            sb.append("Expected IllegalArgumentException not thrown" + newline);
        } catch (IllegalArgumentException iex) {
            sb.append("Expected IllegalArgumentException thrown" + newline);
        } catch (Throwable any) {
            sb.append("Wrong type of Exception thrown" + any.fillInStackTrace());
            pass = false;

        }

        assertTrue(pass, "At least one assertion failed: " + sb);

        TestUtil.logTrace("Test passed. " + sb.toString());
    }

    @Override
    @BeforeEach
    public void cleanup() {
        expected[0] = true;
        expected[1] = false; // Private
        expected[2] = false; // No-cache
        expected[3] = false; // No-Store
        expected[4] = true; // No-Transform
        expected[5] = false; // Must-Revalidate
        expected[6] = false; // ProxyRevalidate
    }

    private static boolean verifyMaxAge(CacheControl ccl, int maxAge, StringBuffer sb) {
        boolean pass = true;
        if (ccl.getMaxAge() != maxAge) {
            pass = false;
            sb.append("set/getMaxAge test failed.  Expecting " + maxAge + ", got " + ccl.getMaxAge() + ".");
        }
        return pass;
    }

    private static boolean verifySMaxAge(CacheControl ccl, int maxAge, StringBuffer sb) {
        boolean pass = true;
        if (ccl.getSMaxAge() != maxAge) {
            pass = false;
            sb.append("set/getMaxAge test failed.  Expecting " + maxAge + ", got " + ccl.getMaxAge() + ".");
        }
        return pass;
    }

    private static boolean verifyList(Object[] expected, CacheControl ccl, StringBuffer sb) {
        boolean pass = true;

        if (ccl.isPrivate() != (Boolean) expected[1]) {
            pass = false;
            sb.append("isPrivate test failed.  Expecting =" + expected[1] + "; Got =" + ccl.isPrivate());
        }
        if (ccl.isNoCache() != (Boolean) expected[2]) {
            pass = false;
            sb.append("isNoCache test failed.  Expecting =" + expected[2] + "; Got =" + ccl.isNoCache());
        }
        if (ccl.isNoStore() != (Boolean) expected[3]) {
            pass = false;
            sb.append("isNoStore test failed.  Expecting =" + expected[3] + "; Got =" + ccl.isNoStore());
        }
        if (ccl.isNoTransform() != (Boolean) expected[4]) {
            pass = false;
            sb.append("isNoTransform test failed.  Expecting =" + expected[4] + "; Got =" + ccl.isNoTransform());
        }
        if (ccl.isMustRevalidate() != (Boolean) expected[5]) {
            pass = false;
            sb.append("isMustRevalidate test failed.  Expecting =" + expected[5] + "; Got =" + ccl.isMustRevalidate());
        }
        if (ccl.isProxyRevalidate() != (Boolean) expected[6]) {
            pass = false;
            sb.append(
                    "isProxyRevalidate test failed.  Expecting =" + expected[6] + "; Got =" + ccl.isProxyRevalidate());
        }
        if (!ccl.getPrivateFields().equals(expected[7])) {
            pass = false;
            sb.append("getPrivateFields test failed.  Expecting =" + expected[7] + "; Got =" + ccl.getPrivateFields());
        }
        if (!ccl.getNoCacheFields().equals(expected[8])) {
            pass = false;
            sb.append("getNoCacheFields test failed.  Expecting =" + expected[8] + "; Got =" + ccl.getNoCacheFields());
        }
        return pass;

    }
}
