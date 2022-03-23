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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateful;

import org.apache.openejb.test.object.ObjectGraph;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import java.rmi.RemoteException;

public class StatefulRmiIiopTests extends StatefulTestClient {

    protected RmiIiopStatefulHome ejbHome;
    protected RmiIiopStatefulObject ejbObject;

    public StatefulRmiIiopTests() {
        super("RMI_IIOP.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/stateful/RMI-over-IIOP/EJBHome");
        ejbHome = (RmiIiopStatefulHome) obj;
        ejbObject = ejbHome.create("RMI-IIOP TestBean");
    }

/*-------------------------------------------------*/
/*  String                                         */
/*-------------------------------------------------*/

    public void test01_returnStringObject() {
        try {
            final String expected = new String("1");
            final String actual = ejbObject.returnStringObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_returnStringObjectArray() {
        try {
            final String[] expected = {"1", "2", "3"};
            final String[] actual = ejbObject.returnStringObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Character                                      */
/*-------------------------------------------------*/

    public void test03_returnCharacterObject() {
        try {
            final Character expected = new Character('1');
            final Character actual = ejbObject.returnCharacterObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_returnCharacterPrimitive() {
        try {
            final char expected = '1';
            final char actual = ejbObject.returnCharacterPrimitive(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_returnCharacterObjectArray() {
        try {
            final Character[] expected = {new Character('1'), new Character('2'), new Character('3')};
            final Character[] actual = ejbObject.returnCharacterObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_returnCharacterPrimitiveArray() {
        try {
            final char[] expected = {'1', '2', '3'};
            final char[] actual = ejbObject.returnCharacterPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Boolean                                        */
/*-------------------------------------------------*/

    public void test07_returnBooleanObject() {
        try {
            final Boolean expected = Boolean.TRUE;
            final Boolean actual = ejbObject.returnBooleanObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test08_returnBooleanPrimitive() {
        try {
            final boolean expected = true;
            final boolean actual = ejbObject.returnBooleanPrimitive(expected);
            assertEquals("" + expected, "" + actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test09_returnBooleanObjectArray() {
        try {
            final Boolean[] expected = {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE};
            final Boolean[] actual = ejbObject.returnBooleanObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test10_returnBooleanPrimitiveArray() {
        try {
            final boolean[] expected = {false, true, true};
            final boolean[] actual = ejbObject.returnBooleanPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Byte                                      */
/*-------------------------------------------------*/

    public void test11_returnByteObject() {
        try {
            final Byte expected = new Byte("1");
            final Byte actual = ejbObject.returnByteObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test12_returnBytePrimitive() {
        try {
            final byte expected = (byte) 1;
            final byte actual = ejbObject.returnBytePrimitive(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test13_returnByteObjectArray() {
        try {
            final Byte[] expected = {new Byte("1"), new Byte("2"), new Byte("3")};
            final Byte[] actual = ejbObject.returnByteObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test14_returnBytePrimitiveArray() {
        try {
            final byte[] expected = {(byte) 1, (byte) 2, (byte) 3};
            final byte[] actual = ejbObject.returnBytePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Short                                      */
/*-------------------------------------------------*/

    public void test15_returnShortObject() {
        try {
            final Short expected = new Short("1");
            final Short actual = ejbObject.returnShortObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test16_returnShortPrimitive() {
        try {
            final short expected = (short) 1;
            final short actual = ejbObject.returnShortPrimitive(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test17_returnShortObjectArray() {
        try {
            final Short[] expected = {new Short("1"), new Short("2"), new Short("3")};
            final Short[] actual = ejbObject.returnShortObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test18_returnShortPrimitiveArray() {
        try {
            final short[] expected = {(short) 1, (short) 2, (short) 3};
            final short[] actual = ejbObject.returnShortPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Integer                                      */
/*-------------------------------------------------*/

    public void test19_returnIntegerObject() {
        try {
            final Integer expected = new Integer(1);
            final Integer actual = ejbObject.returnIntegerObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test20_returnIntegerPrimitive() {
        try {
            final int expected = 1;
            final int actual = ejbObject.returnIntegerPrimitive(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test21_returnIntegerObjectArray() {
        try {
            final Integer[] expected = {new Integer(1), new Integer(2), new Integer(3)};
            final Integer[] actual = ejbObject.returnIntegerObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test22_returnIntegerPrimitiveArray() {
        try {
            final int[] expected = {1, 2, 3};
            final int[] actual = ejbObject.returnIntegerPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Long                                           */
/*-------------------------------------------------*/

    public void test23_returnLongObject() {
        try {
            final Long expected = new Long("1");
            final Long actual = ejbObject.returnLongObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test24_returnLongPrimitive() {
        try {
            final long expected = 1;
            final long actual = ejbObject.returnLongPrimitive(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test25_returnLongObjectArray() {
        try {
            final Long[] expected = {new Long("1"), new Long("2"), new Long("3")};
            final Long[] actual = ejbObject.returnLongObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test26_returnLongPrimitiveArray() {
        try {
            final long[] expected = {1, 2, 3};
            final long[] actual = ejbObject.returnLongPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Float                                      */
/*-------------------------------------------------*/

    public void test27_returnFloatObject() {
        try {
            final Float expected = new Float("1.3");
            final Float actual = ejbObject.returnFloatObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test28_returnFloatPrimitive() {
        try {
            final float expected = 1.2F;
            final float actual = ejbObject.returnFloatPrimitive(expected);
            assertEquals(expected, actual, 0.00D);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test29_returnFloatObjectArray() {
        try {
            final Float[] expected = {new Float("1.1"), new Float("2.2"), new Float("3.3")};
            final Float[] actual = ejbObject.returnFloatObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test30_returnFloatPrimitiveArray() {
        try {
            final float[] expected = {1.2F, 2.3F, 3.4F};
            final float[] actual = ejbObject.returnFloatPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i], 0.0D);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Double                                      */
/*-------------------------------------------------*/

    public void test31_returnDoubleObject() {
        try {
            final Double expected = new Double("1.1");
            final Double actual = ejbObject.returnDoubleObject(expected);
            assertEquals(expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test32_returnDoublePrimitive() {
        try {
            final double expected = 1.2;
            final double actual = ejbObject.returnDoublePrimitive(expected);
            assertEquals(expected, actual, 0.0D);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test33_returnDoubleObjectArray() {
        try {
            final Double[] expected = {new Double("1.3"), new Double("2.4"), new Double("3.5")};
            final Double[] actual = ejbObject.returnDoubleObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test34_returnDoublePrimitiveArray() {
        try {
            final double[] expected = {1.4, 2.5, 3.6};
            final double[] actual = ejbObject.returnDoublePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i], 0.0D);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBHome                                        */
/*-------------------------------------------------*/

    public void test35_returnEJBHome() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome expected = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            final EncStatefulHome actual = (EncStatefulHome) ejbObject.returnEJBHome(expected);
            assertNotNull("The EJBHome returned is null", actual);

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test36_returnEJBHome2() {
        try {
            final EncStatefulHome actual = (EncStatefulHome) ejbObject.returnEJBHome();
            assertNotNull("The EJBHome returned is null", actual);

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test37_returnNestedEJBHome() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome expected = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            final ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            final EncStatefulHome actual = (EncStatefulHome) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test38_returnNestedEJBHome2() {
        try {
            final ObjectGraph graph = ejbObject.returnNestedEJBHome();
            assertNotNull("The ObjectGraph is null", graph);

            final EncStatefulHome actual = (EncStatefulHome) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test39_returnEJBHomeArray() {
        try {

            final EncStatefulHome[] expected = new EncStatefulHome[3];
            for (int i = 0; i < expected.length; i++) {
                final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
                expected[i] = (EncStatefulHome) obj;
                assertNotNull("The EJBHome returned from JNDI is null", expected[i]);
            }

            final EJBHome[] actual = ejbObject.returnEJBHomeArray(expected);
            assertNotNull("The EJBHome array returned is null", actual);
            assertEquals(expected.length, actual.length);

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBObject                                      */
/*-------------------------------------------------*/

    public void test40_returnEJBObject() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject expected = home.create("test_40 StatefulBean");
            assertNotNull("The EJBObject created is null", expected);

            final EncStatefulObject actual = (EncStatefulObject) ejbObject.returnEJBObject(expected);
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test41_returnEJBObject2() {
        try {
            final EncStatefulObject actual = (EncStatefulObject) ejbObject.returnEJBObject();
            assertNotNull("The EJBObject returned is null", actual);

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test42_returnNestedEJBObject() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject expected = home.create("test_42 StatefulBean");
            assertNotNull("The EJBObject created is null", expected);

            final ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            final EncStatefulObject actual = (EncStatefulObject) graph.getObject();
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test43_returnNestedEJBObject2() {
        try {
            final ObjectGraph graph = ejbObject.returnNestedEJBObject();
            assertNotNull("The ObjectGraph is null", graph);

            final EncStatefulObject actual = (EncStatefulObject) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test44_returnEJBObjectArray() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject[] expected = new EncStatefulObject[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = home.create("test_44 StatefulBean");
                assertNotNull("The EJBObject created is null", expected[i]);
            }

            final EJBObject[] actual = ejbObject.returnEJBObjectArray(expected);
            assertNotNull("The EJBObject array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < actual.length; i++) {
                assertTrue("The EJBObejcts are not identical", expected[i].isIdentical(actual[i]));
            }

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

/*-------------------------------------------------*/
/*  EJBMetaData                                    */
/*-------------------------------------------------*/

    public void test45_returnEJBMetaData() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            final EJBMetaData actual = ejbObject.returnEJBMetaData(expected);
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test46_returnEJBMetaData() {
        try {
            final EJBMetaData actual = ejbObject.returnEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(actual.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(actual.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test47_returnNestedEJBMetaData() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            final ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            final EJBMetaData actual = (EJBMetaData) graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test48_returnNestedEJBMetaData2() {
        try {
            final ObjectGraph graph = ejbObject.returnNestedEJBMetaData();
            assertNotNull("The ObjectGraph is null", graph);

            final EJBMetaData actual = (EJBMetaData) graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertNotNull("The home interface class of the EJBMetaData is null", actual.getHomeInterfaceClass());
            assertNotNull("The remote interface class of the EJBMetaData is null", actual.getRemoteInterfaceClass());
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test49_returnEJBMetaDataArray() {
        try {

            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EJBMetaData[] expected = new EJBMetaData[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = home.getEJBMetaData();
                assertNotNull("The EJBMetaData returned is null", expected[i]);
            }

            final EJBMetaData[] actual = (EJBMetaData[]) ejbObject.returnEJBMetaDataArray(expected);
            assertNotNull("The EJBMetaData array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < actual.length; i++) {
                assertNotNull("The EJBMetaData returned is null", actual[i]);
                assertEquals(expected[i].getHomeInterfaceClass(), actual[i].getHomeInterfaceClass());
                assertEquals(expected[i].getRemoteInterfaceClass(), actual[i].getRemoteInterfaceClass());
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Handle                                         */
/*-------------------------------------------------*/

    public void test50_returnHandle() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject object = home.create("test_50 StatefulBean");
            assertNotNull("The EJBObject created is null", object);

            final Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            final Handle actual = ejbObject.returnHandle(expected);
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            final EJBObject exp = expected.getEJBObject();
            final EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test51_returnHandle() {
        try {
            final Handle actual = ejbObject.returnHandle();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test52_returnNestedHandle() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject object = home.create("test_52 StatefulBean");
            assertNotNull("The EJBObject created is null", object);

            final Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            final ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            final Handle actual = (Handle) graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            final EJBObject exp = expected.getEJBObject();
            final EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test53_returnNestedHandle2() {
        try {
            final ObjectGraph graph = ejbObject.returnNestedHandle();
            assertNotNull("The ObjectGraph is null", graph);

            final Handle actual = (Handle) graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test54_returnHandleArray() {
        try {
            final Object obj = initialContext.lookup("client/tests/stateful/EncBean");
            final EncStatefulHome home = (EncStatefulHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            final EncStatefulObject object = home.create("test_54 StatefulBean");
            assertNotNull("The EJBObject created is null", object);

            final Handle[] expected = new Handle[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = object.getHandle();
                assertNotNull("The EJBObject Handle returned is null", expected[i]);
            }

            final Handle[] actual = (Handle[]) ejbObject.returnHandleArray(expected);
            assertNotNull("The Handle array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < expected.length; i++) {
                assertNotNull("The EJBObject Handle returned is null", actual[i]);
                assertNotNull("The EJBObject in the Handle is null", actual[i].getEJBObject());
                assertTrue("The EJBObjects in the Handles are not equal", expected[i].getEJBObject().isIdentical(actual[i].getEJBObject()));
            }

        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Foo                                      */
/*-------------------------------------------------*/

    public void test55_returnObjectGraph() {
    }

    public void test56_returnObjectGraphArray() {
    }

/*-------------------------------------------------*/
/*  Class                                          */
/*-------------------------------------------------*/

    public void test57_returnClass() {
        final Class[] primitives = {boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class};
        for (final Class expected : primitives) {
            try {
                final Class actual = ejbObject.returnClass(expected);
                assertEquals(expected, actual);
            } catch (final Exception e) {
                fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        }
    }

    public void test58_returnClassArray() {
        try {
            final Class[] expected = {boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class};
            final Class[] actual = ejbObject.returnClassArray(expected);

            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], actual[i]);
            }
        } catch (final RemoteException e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test59_returnClassObjectGraph() {
        try {
            final Class[] primitives = {boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class};
            final ObjectGraph expectedGraph = new ObjectGraph(primitives);

            final ObjectGraph actualGraph = ejbObject.returnObjectGraph(expectedGraph);

            final Class[] expected = (Class[]) expectedGraph.getObject();
            final Class[] actual = (Class[]) actualGraph.getObject();

            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], actual[i]);
            }
        } catch (final RemoteException e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

}

