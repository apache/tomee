/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.form;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = 1L;

    protected static final String tck = "tck";

    protected static final String cts = "cts";

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /*
     * @testName: constructorNoArgTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:765; JAXRS:JAVADOC:766;
     * 
     * @test_Strategy: Returns multivalued map representation of the form.
     * 
     * Create a new form data instance.
     */
    @Test
    public Form constructorNoArgTest() throws Fault {
        Form form = new Form();
        assertTrue(form != null, "No Form created");

        MultivaluedMap<String, String> map = form.asMap();
        assertTrue(map.isEmpty(), "Created From instance is not empty");
        logMsg("Form instance created");
        return form;
    }

    /*
     * @testName: constructorStringArgsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:767; JAXRS:JAVADOC:765;
     * 
     * @test_Strategy: Create a new form data instance with a single parameter
     * entry.
     */
    @Test
    public void constructorStringArgsTest() throws Fault {
        Form form = new Form(tck, cts);
        assertTrue(form != null, "No Form created");

        MultivaluedMap<String, String> map = form.asMap();
        assertTrue(map.containsKey(tck), "No given key " + tck + " exists in form instance");
        assertTrue(map.getFirst(tck).equals(cts),
                "Different value has been given from map for key " + tck + ": " + map.getFirst(tck));
        logMsg("Form instance with String arguments sucessfully created");
    }

    /*
     * @testName: constructorMultivaluedMapArgTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:768; JAXRS:JAVADOC:765;
     * 
     * @test_Strategy: Create a new form data instance with a single parameter
     * entry.
     */
    @Test
    public void constructorMultivaluedMapArgTest() throws Fault {
        MultivaluedHashMap<String, String> init = new MultivaluedHashMap<String, String>();
        init.add(tck, cts);
        init.add(cts, cts);
        Form form = new Form(init);
        assertTrue(form != null, "No Form created");

        MultivaluedMap<String, String> map = form.asMap();
        assertTrue(map.containsKey(tck), "No given key " + tck + " exists in form instance");
        assertTrue(map.getFirst(tck).equals(cts),
                "Different value has been given from map for key " + tck + ": " + map.getFirst(tck));
        assertTrue(map.containsKey(cts), "No given key " + cts + " exists in form instance");
        assertTrue(map.getFirst(cts).equals(cts),
                "Different value has been given from map for key " + cts + ": " + map.getFirst(cts));
        logMsg("Form instance with MultivaluedMap argument sucessfully created");
    }

    /*
     * @testName: paramTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:765; JAXRS:JAVADOC:769;
     * 
     * @test_Strategy: Returns multivalued map representation of the form.
     * 
     * Adds a new value to the specified form parameter.
     */
    @Test
    public void paramTest() throws Fault {
        Form form = constructorNoArgTest();
        form.param(tck, tck);
        form.param(cts, cts);

        MultivaluedMap<String, String> map = form.asMap();
        assertTrue(map.containsKey(tck), "No given key " + tck + "exists in form instance");
        assertTrue(map.getFirst(tck).equals(tck),
                "Different value has been given from map for key " + tck + ": " + map.getFirst(tck));
        assertTrue(map.containsKey(cts), "No given key " + cts + " exists in form instance");
        assertTrue(map.getFirst(cts).equals(cts),
                "Different value has been given from map for key " + cts + ": " + map.getFirst(cts));
    }
}
