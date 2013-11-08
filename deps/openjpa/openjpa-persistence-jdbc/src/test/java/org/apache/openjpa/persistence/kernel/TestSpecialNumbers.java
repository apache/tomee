/*
 * TestSpecialNumbers.java
 *
 * Created on October 13, 2006, 4:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.kernel;

import java.math.BigInteger;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.openjpa.persistence.kernel.common.apps.AllFieldTypesTest;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import junit.framework.AssertionFailedError;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestSpecialNumbers extends BaseKernelTest {

    /**
     * Creates a new instance of TestSpecialNumbers
     */
    public TestSpecialNumbers() {
    }

    public TestSpecialNumbers(String name) {
        super(name);
    }

    public void setUp() {
        try {
            deleteAll(AllFieldTypesTest.class);
        } catch (Exception e) {
            // catch errors when deleting ... PostgreSQL has
            // a couple problems with some of the values that
            // try to get inserted.
        }
    }

    public void testShortMax() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestshort(Short.MAX_VALUE);
        saveAndQuery(aftt, "testshort = :param", new Short(Short.MAX_VALUE));
    }

    public void testShortMin() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestshort(Short.MIN_VALUE);
        try {
            saveAndQuery(aftt, "testshort = :param",
                new Short(Short.MIN_VALUE));
        } catch (Throwable t) {
            bug(AbstractTestCase.Platform.EMPRESS, 889, t,
                "Empress cannot store min values");
        }
    }

    public void testLongMax() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestlong(Long.MAX_VALUE);
        saveAndQuery(aftt, "testlong = :param", new Long(Long.MAX_VALUE));
    }

    public void testLongMin() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestlong(Long.MIN_VALUE);
            saveAndQuery(aftt, "testlong = :param", new Long(Long.MIN_VALUE));
        } catch (Throwable t) {
            bug(AbstractTestCase.Platform.HYPERSONIC, 474, t,
                "Some databases cannot store Long.MIN_VALUE");
        }
    }

    public void testIntegerMax() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestint(Integer.MAX_VALUE);
        saveAndQuery(aftt, "testint = :param",
            new Integer(Integer.MAX_VALUE));
    }

    public void testIntegerMin() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestint(Integer.MIN_VALUE);
        try {
            saveAndQuery(aftt, "testint = :param",
                new Integer(Integer.MIN_VALUE));
        } catch (Throwable t) {
            bug(AbstractTestCase.Platform.EMPRESS, 889, t,
                "Empress cannot store min values");
        }
    }

    public void testFloatMax() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestfloat(Float.MAX_VALUE);
            saveAndQuery(aftt, "testfloat = :param",
                new Float(Float.MAX_VALUE));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.MARIADB,
                AbstractTestCase.Platform.MYSQL,
                AbstractTestCase.Platform.DERBY), 494, e,
                "Some datastores cannot store Float.MAX_VALUE");
        }
    }

    public void testFloatMin() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestfloat(Float.MIN_VALUE);
            saveAndQuery(aftt, "testfloat = :param",
                new Float(Float.MIN_VALUE));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.SQLSERVER,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.DERBY), 494, e,
                "Some databases cannot store Float.MIN_VALUE");
        } catch (AssertionFailedError e) {
            bug(EnumSet.of(AbstractTestCase.Platform.MARIADB, AbstractTestCase.Platform.MYSQL,
                AbstractTestCase.Platform.SQLSERVER), 494, e,
                "Some databases cannot store Float.MIN_VALUE");
        }
    }

    public void testFloatNaN() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestfloat(Float.NaN);
            saveAndQuery(aftt, "testfloat = :param", new Float(Float.NaN));
        } catch (Throwable t) {
            bug(461, t, "NaN problems");
        }
    }

    public void testFloatNegativeInfinity() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestfloat(Float.NEGATIVE_INFINITY);
            saveAndQuery(aftt, "testfloat = :param",
                new Float(Float.NEGATIVE_INFINITY));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.MARIADB,
                AbstractTestCase.Platform.MYSQL,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.ORACLE,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.DERBY,
                AbstractTestCase.Platform.SQLSERVER), 494, e,
                "Some databases cannot store Float.NEGATIVE_INFINITY");
        }
    }

    public void testFloatPostivieInfinity() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestfloat(Float.POSITIVE_INFINITY);
            saveAndQuery(aftt, "testfloat = :param",
                new Float(Float.POSITIVE_INFINITY));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.MARIADB,
                AbstractTestCase.Platform.MYSQL,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.ORACLE,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.DERBY,
                AbstractTestCase.Platform.SQLSERVER), 494, e,
                "Some databases cannot store Float.POSITIVE_INFINITY");
        }
    }

    public void testDoubleMax() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestdouble(Double.MAX_VALUE);
            saveAndQuery(aftt, "testdouble = :param",
                new Double(Double.MAX_VALUE));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.MARIADB,
                AbstractTestCase.Platform.MYSQL,
                AbstractTestCase.Platform.ORACLE,
                AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.EMPRESS,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.DERBY), 494, e,
                "Some databases cannot store Double.MAX_VALUE");
        }
    }

    public void testDoubleMin() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestdouble(Double.MIN_VALUE);
            saveAndQuery(aftt, "testdouble = :param",
                new Double(Double.MIN_VALUE));
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POSTGRESQL,
                AbstractTestCase.Platform.SQLSERVER,
                AbstractTestCase.Platform.ORACLE,
                AbstractTestCase.Platform.EMPRESS,
                AbstractTestCase.Platform.DB2,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.DERBY), 494, e,
                "Some databases cannot store Double.MIN_VALUE");
        } catch (AssertionFailedError e) {
            bug(EnumSet.of(AbstractTestCase.Platform.MARIADB, AbstractTestCase.Platform.MYSQL), 494, e,
                "Some databases cannot store Double.MIN_VALUE");
        }
    }

    public void testDoubleNaN() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestdouble(Double.NaN);
            saveAndQuery(aftt, "testdouble = :param", new Double(Double.NaN));
        } catch (Throwable t) {
            bug(461, t, "NaN problems");
        }
    }

    public void testDoubleNegativeInfinity() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestdouble(Double.NEGATIVE_INFINITY);
            saveAndQuery(aftt, "testdouble = :param",
                new Double(Double.NEGATIVE_INFINITY));
        } catch (Throwable t) {
            bug(461, t, "infinity problems");
        }
    }

    public void testDoublePostivieInfinity() {
        try {
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestdouble(Double.POSITIVE_INFINITY);
            saveAndQuery(aftt, "testdouble = :param",
                new Double(Double.POSITIVE_INFINITY));
        } catch (Throwable t) {
            bug(461, t, "infinity problems");
        }
    }

    public void testByteMin() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestbyte(Byte.MIN_VALUE);
        try {
            saveAndQuery(aftt, "testbyte = :param", new Byte(Byte.MIN_VALUE));
        } catch (Throwable t) {
            bug(AbstractTestCase.Platform.EMPRESS, 889, t,
                "Empress cannot store min values");
        }
    }

    public void testByteMax() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestbyte(Byte.MAX_VALUE);
        saveAndQuery(aftt, "testbyte = :param", new Byte(Byte.MAX_VALUE));
    }

    public void testZeroBigInteger() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestBigInteger(BigInteger.ZERO);
        saveAndQuery(aftt, "testBigInteger = :param", BigInteger.ZERO);
    }

    public void testOneBigInteger() {
        AllFieldTypesTest aftt = new AllFieldTypesTest();
        aftt.setTestBigInteger(BigInteger.ONE);
        saveAndQuery(aftt, "testBigInteger = :param", BigInteger.ONE);
    }

    private void saveAndQuery(Object obj, String query, Object param) {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(obj);
        endTx(pm);
        endEm(pm);

        pm = getPM();
        OpenJPAQuery q = pm.createQuery("select o from "
            + obj.getClass().getName() + " o where " + query);
        q.setParameter("param", param);
        Collection c = (Collection) q.getResultList();
        assertSize(1, c);
        endEm(pm);
    }
}
