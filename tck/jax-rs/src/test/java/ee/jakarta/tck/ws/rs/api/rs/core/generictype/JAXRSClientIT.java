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

package ee.jakarta.tck.ws.rs.api.rs.core.generictype;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.GenericType;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = 1L;

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /*
     * @testName: constructorWithTypeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:774; JAXRS:JAVADOC:775; JAXRS:JAVADOC:776;
     * 
     * @test_Strategy: Constructs a new generic type, supplying the generic type
     * information and deriving the class.
     */
    @Test
    public void constructorWithTypeTest() throws Fault {
        Method method = null;
        try {
            // obtain the correct return type
            method = getClass().getMethod("dummyMethod");
        } catch (Exception e) {
            throw new Fault(e);
        }

        GenericType<List<String>> type = new GenericType<List<String>>(method.getReturnType());
        assertTrue(type != null, "Could not create a GenericType instance");
        assertTrue(type.getRawType().isAssignableFrom(ArrayList.class), type.getRawType() + " != " + List.class);
        assertTrue(type.getType().toString().contains(ArrayList.class.getName()),
                type.getType() + " != " + ArrayList.class);
        logMsg("Succesfully created GenericType<ArrayList<String>>(Type) instance");
    }

    // * NEED for reflection in constructorWith2ArgsTest */
    public ArrayList<String> dummyMethod() {
        return null;
    }

    /*
     * @testName: constructorProtectedTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:774; JAXRS:JAVADOC:775; JAXRS:JAVADOC:776;
     * 
     * @test_Strategy: Constructs a new generic type, deriving the generic type and
     * class from type parameter.
     */
    @Test
    public void constructorProtectedTest() throws Fault {
        GenericType<TreeSet<String>> type = new GenericType<TreeSet<String>>() {
        };
        assertTrue(type != null, "Could not create a GenericType instance");
        assertTrue(type.getRawType().isAssignableFrom(TreeSet.class), type.getRawType() + " != " + Set.class);
        assertTrue(type.getType().toString().contains(TreeSet.class.getName()),
                type.getType() + " != " + TreeSet.class);
        logMsg("Succesfully created GenericType<TreeSet<String>>(){} instance");
    }

    /*
     * @testName: equalsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:773;
     * 
     * @test_Strategy: Two GenericType<TreeSet<String>> must be the equal
     */
    @Test
    public void equalsTest() throws Fault {
        GenericType<TreeSet<String>> type1 = new GenericType<TreeSet<String>>() {
        };
        GenericType<TreeSet<String>> type2 = new GenericType<TreeSet<String>>() {
        };
        GenericType<Set<String>> type3 = new GenericType<Set<String>>() {
        };

        assertTrue(type1.equals(type1), "GenericType<TreeSet<String>> is not equal itself");
        assertTrue(type2.equals(type2), "GenericType<TreeSet<String>> is not equal itself");
        assertTrue(type1.equals(type2), "GenericType<TreeSet<String>> is not equal GenericType<TreeSet<String>>");
        assertTrue(type2.equals(type1), "GenericType<TreeSet<String>> is not equal GenericType<TreeSet<String>>");
        assertTrue(!type3.equals(type1), "GenericType<Set<String>> is equal GenericType<TreeSet<String>>");
        logMsg("The tested GenericType<TreeSet<String>> instances are equal");
    }

    /*
     * @testName: hashCodeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:777;
     * 
     * @test_Strategy: hashCode of two GenericType<TreeSet<String>> must be the same
     */
    @Test
    public void hashCodeTest() throws Fault {
        GenericType<TreeSet<String>> type1 = new GenericType<TreeSet<String>>() {
        };
        GenericType<TreeSet<String>> type2 = new GenericType<TreeSet<String>>() {
        };
        GenericType<Set<String>> type3 = new GenericType<Set<String>>() {
        };

        assertTrue(type1.hashCode() == type1.hashCode(), "HashCode of itself is random");
        assertTrue(type2.hashCode() == type2.hashCode(), "HashCode of itself is random");
        assertTrue(type1.hashCode() == type2.hashCode(), "Both GenericType instances should have the same hashCode");
        assertTrue(type1.hashCode() != type3.hashCode(),
                "GenericType<Set<String>>.hashCode()==GenericType<TreeSet<String>>.hashCode()");

        logMsg("Both GenericType instances have the same hashCode()");
    }

    /*
     * @testName: toStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:778;
     * 
     * @test_Strategy: toString of two GenericType<TreeSet<String>> must be the same
     */
    @Test
    public void toStringTest() throws Fault {
        GenericType<TreeSet<String>> type1 = new GenericType<TreeSet<String>>() {
        };
        GenericType<TreeSet<String>> type2 = new GenericType<TreeSet<String>>() {
        };
        GenericType<Set<String>> type3 = new GenericType<Set<String>>() {
        };

        assertTrue(type1.toString().equals(type1.toString()), "toString() of itself is random");
        assertTrue(type2.toString().equals(type2.toString()), "toString() of itself is random");
        assertTrue(type1.toString().equals(type2.toString()),
                "Both GenericType instances should have the same toString()");
        assertTrue(!type1.toString().equals(type3.toString()),
                "GenericType<Set<String>>.toString()==GenericType<TreeSet<String>>.toString()");

        logMsg("Both GenericType instances have the same toString()", type1);
    }
}
