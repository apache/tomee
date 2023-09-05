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

package ee.jakarta.tck.ws.rs.api.rs.core.genericentity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.GenericEntity;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
* @class.setup_props: webServerHost; webServerPort; ts_home;
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
     * @testName: constructorTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:71;
     * 
     * @test_Strategy: Create a GenericEntity instance using GenericEntity(T entity,
     * null) Verify the expected IllegalArgumentException is thrown.
     */
    @Test
    public void constructorTest1() throws Fault {
        List<String> list = new ArrayList<String>();
        try {
            new GenericEntity<List<String>>(list, null);
            throw new Fault("Test failed; expected exception not thrown.");
        } catch (IllegalArgumentException ilex) {
            logMsg("Expected IllegalArgumentException thrown:", ilex.getMessage());
        }
    }

    /*
     * @testName: constructorTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:71;
     * 
     * @test_Strategy: Create a GenericEntity instance using GenericEntity(null,
     * Type) Verify the expected IllegalArgumentException is thrown.
     */
    @Test
    public void constructorTest2() throws Fault {
        try {
            new GenericEntity<List<String>>(null, String.class.getGenericSuperclass());
            throw new Fault("Test failed; expected exception not thrown.");
        } catch (IllegalArgumentException ilex) {
            logMsg("Expected IllegalArgumentException thrown:", ilex.getMessage());
        }
    }

    /*
     * @testName: singleArgumentConstructorTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:71;
     * 
     * @test_Strategy: Create a GenericEntity instance using GenericEntity(T entity)
     * Verify the expected IllegalArgumentException is thrown for null and not for
     * legal argument
     */
    @Test
    public void singleArgumentConstructorTest() throws Fault {
        try {
            new GenericEntity<Map<String, List<Long>>>(null) {
            };
            throw new Fault("Test failed; expected exception not thrown.");
        } catch (IllegalArgumentException ilex) {
            logMsg("Expected IllegalArgumentException thrown:", ilex.getMessage());
        }
        GenericEntity<Map<String, List<Long>>> generic;
        generic = new GenericEntity<Map<String, List<Long>>>(new java.util.HashMap<String, List<Long>>()) {
        };
        assertTrue(generic.getRawType().isAssignableFrom(HashMap.class), generic.getRawType() + " != " + Map.class);
        logMsg("GenericEntity<Map<String, List<Long>>> instance created");
    }

    /*
     * @testName: constructorWith2ArgsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:71;
     * 
     * @test_Strategy: Create a GenericEntity instance using GenericEntity(T entity,
     * not null)
     */
    @Test
    public void constructorWith2ArgsTest() throws Fault {
        List<String> list = new ArrayList<String>();
        GenericEntity<List<String>> generic;
        Method method = null;
        try {
            // obtain the correct return type
            method = getClass().getMethod("dummyMethod");
        } catch (Exception e) {
            throw new Fault(e);
        }
        generic = new GenericEntity<List<String>>(list, method.getGenericReturnType());
        String str = String.class.getSimpleName();
        assertTrue(generic.getRawType().isAssignableFrom(ArrayList.class), generic.getRawType() + " != " + List.class);
        assertTrue(generic.getType().toString().contains(str), generic.getType() + ".contains(" + str + ") != true");
        logMsg("GenericEntity<List<String>>(List, Type) instance created");
    }

    /*
     * @testName: constructorWith1ArgTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:71;
     * 
     * @test_Strategy: Create a GenericEntity instance using GenericEntity(T entity)
     */
    @Test
    public void constructorWith1ArgTest() throws Fault {
        List<String> list = new ArrayList<String>();
        GenericEntity<List<String>> generic;
        generic = new GenericEntity<List<String>>(list) {
        };
        String str = String.class.getSimpleName();
        assertTrue(generic.getRawType() == ArrayList.class, generic.getRawType() + " != " + List.class);
        assertTrue(generic.getType().toString().contains(str), generic.getType() + ".contains(" + str + ") != true");
        logMsg("GenericEntity<List<String>>(List) instance created");
    }

    // * NEED for reflection in constructorWith2ArgsTest */
    public List<String> dummyMethod() {
        return null;
    }

    /*
     * @testName: getEntityTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:72;
     * 
     * @test_Strategy: Create GenericEntity with a map, retrieve and validate
     */
    @Test
    public void getEntityTest() throws Fault {
        String testName = "EntityTest";
        Map<String, List<Long>> map = new java.util.HashMap<String, List<Long>>();
        map.put(testName, new java.util.LinkedList<Long>());

        GenericEntity<Map<String, List<Long>>> ge = new GenericEntity<Map<String, List<Long>>>(map) {
        };

        if (ge.getEntity() != map || !ge.getEntity().keySet().iterator().next().equals(testName))
            throw new Fault("Entity has not been retrieved");
        logMsg("Entity has been retrieved");
    }

    /*
     * @testName: getTypeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:74;
     * 
     * @test_Strategy: Create a GenericEntity instance; Verify that
     * GenericEntity.getType() works.
     */
    @Test
    public void getTypeTest() throws Fault {
        Boolean pass = true;
        StringBuffer sb = new StringBuffer();

        List<String> lists = new ArrayList<String>();
        GenericEntity<List<String>> entitys = new GenericEntity<List<String>>(lists) {
        };

        String types = entitys.getType().toString();
        if (types.equals("java.util.List<java.lang.String>")) {
            sb.append("getType return correctly: ").append(types).append(newline);
        } else {
            pass = false;
            sb.append("getType return incorrectly: ").append(types).append(newline)
                    .append("Expecting java.util.List<java.lang.String>.").append(newline);
        }

        List<Integer> listi = new ArrayList<Integer>();
        GenericEntity<List<Integer>> entityi = new GenericEntity<List<Integer>>(listi) {
        };

        String typei = entityi.getType().toString();
        if (typei.equals("java.util.List<java.lang.Integer>")) {
            sb.append("getType return correctly: ").append(types).append(newline);
        } else {
            pass = false;
            sb.append("getType return incorrectly: ").append(typei).append(newline)
                    .append("Expecting java.util.List<java.lang.Integer>.").append(newline);
        }

        if (pass) {
            logMsg("Test passed. ", sb.toString());
        } else {
            throw new Fault("At least one assertion falied. " + sb.toString());
        }
    }

    /*
     * @testName: getRawTypeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:73;
     * 
     * @test_Strategy: Create a GenericEntity instance; Verify that
     * GenericEntity.getRawType() works.
     */
    @Test
    public void getRawTypeTest() throws Fault {
        Boolean pass = true;
        StringBuffer sb = new StringBuffer();

        List<String> lists = new ArrayList<String>();
        GenericEntity<List<String>> entitys = new GenericEntity<List<String>>(lists) {
        };

        String types = entitys.getRawType().toString();
        if (types.indexOf("java.util.ArrayList") > -1) {
            sb.append("getType return correctly: ").append(types).append(newline);
        } else {
            pass = false;
            sb.append("getType return incorrectly: ").append(types).append(newline);
            sb.append("Expecting java.util.ArrayList.").append(newline);
        }

        List<Integer> listi = new ArrayList<Integer>();
        GenericEntity<List<Integer>> entityi = new GenericEntity<List<Integer>>(listi) {
        };

        String typei = entityi.getRawType().toString();
        if (typei.indexOf("java.util.ArrayList") > -1) {
            sb.append("getType return correctly: ").append(types).append(newline);
        } else {
            pass = false;
            sb.append("getType return incorrectly: ").append(typei).append(newline);
            sb.append("Expecting java.util.ArrayList.").append(newline);
        }

        if (pass) {
            logMsg("Test passed.", sb.toString());
        } else {
            throw new Fault("At least one assertion falied. " + sb.toString());
        }
    }

    /*
     * @testName: equalsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:770;
     * 
     * @test_Strategy: Two GenericEntity<TreeSet<String>> must be the equal
     */
    @Test
    public void equalsTest() throws Fault {
        TreeSet<String> set = new TreeSet<String>();

        GenericEntity<TreeSet<String>> type1 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type2 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<Set<String>> type3 = new GenericEntity<Set<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type4 = new GenericEntity<TreeSet<String>>(new TreeSet<String>() {
            private static final long serialVersionUID = 1L;
        }) {
        };

        assertTrue(type1.equals(type1), "GenericEntity<TreeSet<String>> is not equal itself");
        assertTrue(type2.equals(type2), "GenericEntity<TreeSet<String>> is not equal itself");
        assertTrue(type1.equals(type2), "GenericEntity<TreeSet<String>> is not equal GenericEntity<TreeSet<String>>");
        assertTrue(type2.equals(type1), "GenericEntity<TreeSet<String>> is not equal GenericEntity<TreeSet<String>>");
        assertTrue(!type3.equals(type1), "GenericEntity<Set<String>> is equal GenericEntity<TreeSet<String>>");
        assertTrue(type4.equals(type1),
                "GenericEntity<TreeSet<String>>(set) is not equal GenericEntity<TreeSet<String>>(otherSet)");
        logMsg("The tested GenericEntity<TreeSet<String>> instances are equal");
    }

    /*
     * @testName: hashCodeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:771;
     * 
     * @test_Strategy: HashCode of two GenericEntity<TreeSet<String>> must be the
     * same
     */
    @Test
    public void hashCodeTest() throws Fault {
        TreeSet<String> set = new TreeSet<String>();
        GenericEntity<TreeSet<String>> type1 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type2 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<Set<String>> type3 = new GenericEntity<Set<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type4 = new GenericEntity<TreeSet<String>>(new TreeSet<String>() {
            private static final long serialVersionUID = 1L;
        }) {
        };

        assertTrue(type1.hashCode() == type1.hashCode(), "HashCode of itself is random");
        assertTrue(type2.hashCode() == type2.hashCode(), "HashCode of itself is random");
        assertTrue(type1.hashCode() == type2.hashCode(), "Both GenericEntity instances should have the same hashCode");
        assertTrue(type1.hashCode() != type3.hashCode(),
                "GenericEntity<Set<String>>.hashCode()==GenericEntity<TreeSet<String>>.hashCode()");
        assertTrue(type4.hashCode() == type1.hashCode(),
                "GenericEntity<Set<String>>(set).hashCode()!=GenericEntity<TreeSet<String>>(otherSet).hashCode()");

        logMsg("Both GenericEntity instances have the same hashCode()");
    }

    /*
     * @testName: toStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:772;
     * 
     * @test_Strategy: toString() of two GenericEntity<TreeSet<String>> must be the
     * same
     */
    @Test
    public void toStringTest() throws Fault {
        TreeSet<String> set = new TreeSet<String>();
        GenericEntity<TreeSet<String>> type1 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type2 = new GenericEntity<TreeSet<String>>(set) {
        };
        GenericEntity<Set<String>> type3 = new GenericEntity<Set<String>>(set) {
        };
        GenericEntity<TreeSet<String>> type4 = new GenericEntity<TreeSet<String>>(new TreeSet<String>() {
            private static final long serialVersionUID = 1L;
        }) {
        };

        assertTrue(type1.toString().equals(type1.toString()), "toString() of itself is random");
        assertTrue(type2.toString().equals(type2.toString()), "toString() of itself is random");
        assertTrue(type1.toString().equals(type2.toString()),
                "Both GenericEntity instances should have the same toString()");
        assertTrue(!type1.toString().equals(type3.toString()),
                "GenericEntity<Set<String>>.toString()==GenericEntity<TreeSet<String>>.toString()");
        assertTrue(type4.toString().equals(type1.toString()),
                "GenericEntity<Set<String>>(set).toString()!=GenericEntity<TreeSet<String>>(otherSet).toString()");

        logMsg("Both GenericEntity instances have the same toString()", type4);
    }
}
