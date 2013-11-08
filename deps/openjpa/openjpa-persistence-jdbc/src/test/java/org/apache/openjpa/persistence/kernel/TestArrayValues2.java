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

import java.lang.reflect.Array;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;


import org.apache.openjpa.persistence.kernel.common.apps.ArraysTest;
import junit.framework.AssertionFailedError;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * Test varying kinds of array values.
 *
 * @author Marc Prud'hommeaux
 * @author Abe White
 */
public class TestArrayValues2 extends BaseKernelTest {

    private static double DOUBLE_PRECISION = 0.01D;
    public static float FLOAT_PRECISION = 0.01F;

    public void testStringArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        String[] array = new String[max];
        for (int i = 0; i < max; i++)
            array[i] = randomString();
        saveArray(array, false, false);
    }

    public void testLongArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Long[] array = new Long[max];
        for (int i = 0; i < max; i++)
            array[i] = randomLong();
        try {
            saveArray(array, false, false);
        } catch (AssertionFailedError afe) {
        }
    }

    public void testLongPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        long[] array = new long[max];
        for (int i = 0; i < max; i++)
            array[i] = randomLong().longValue();
        try {
            saveArray(array, true, false);
        } catch (AssertionFailedError afe) {
        }
    }

    public void testShortArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Short[] array = new Short[max];
        for (int i = 0; i < max; i++)
            array[i] = randomShort();
        saveArray(array, false, false);
    }

    public void testShortPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        short[] array = new short[max];
        for (int i = 0; i < max; i++)
            array[i] = randomShort().shortValue();
        saveArray(array, true, false);
    }

    public void testIntArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Integer[] array = new Integer[max];
        for (int i = 0; i < max; i++)
            array[i] = randomInt();
        saveArray(array, false, false);
    }

    public void testIntPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        int[] array = new int[max];
        for (int i = 0; i < max; i++)
            array[i] = randomInt().intValue();
        saveArray(array, true, false);
    }
// FixMe: Both tests hangs on Mysql
    /*public void testByteArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Byte[] array = new Byte[max];
        for (int i = 0; i < max; i++)
            array[i] = randomByte();
        saveArray(array, false, false);
    }

    public void testBytePrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        byte[] array = new byte[max];
        for (int i = 0; i < max; i++)
            array[i] = randomByte().byteValue();
        saveArray(array, true, false);
    }*/

    public void testBooleanArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Boolean[] array = new Boolean[max];
        for (int i = 0; i < max; i++)
            array[i] = randomBoolean();
        saveArray(array, false, false);
    }

    public void testCharacterArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Character[] array = new Character[max];
        array[0] = new Character((char) 1);
        for (int i = 1; i < max; i++)
            array[i] = randomChar();
        saveArray(array, false, false);
    }

    public void testCharacterPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        char[] array = new char[max];
        array[0] = 1;
        for (int i = 1; i < max; i++)
            array[i] = randomChar().charValue();
        saveArray(array, true, false);
    }

    public void testCharacterPrimitiveClobArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        char[] array = new char[max];
        array[0] = 1;
        for (int i = 1; i < max; i++)
            array[i] = randomChar().charValue();
        saveArray(array, true, true);
    }

    public void testBooleanPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        boolean[] array = new boolean[max];
        for (int i = 0; i < max; i++)
            array[i] = randomBoolean().booleanValue();
        saveArray(array, true, false);
    }

    public void testFloatArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Float[] array = new Float[max];
        for (int i = 0; i < max; i++)
            array[i] = randomFloat();
        saveArray(array, false, false);
    }

    public void testFloatPrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        float[] array = new float[max];
        for (int i = 0; i < max; i++)
            array[i] = randomFloat().floatValue();
        saveArray(array, true, false);
    }

    public void testDoubleArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Double[] array = new Double[max];
        for (int i = 0; i < max; i++)
            array[i] = randomDouble();
        saveArray(array, false, false);
    }

    public void testDoublePrimitiveArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        double[] array = new double[max];
        for (int i = 0; i < max; i++)
            array[i] = randomDouble().doubleValue();
        saveArray(array, true, false);
    }

    public void testDateArray()
        throws Exception {
        int max = ((int) (Math.random() * 20)) + 5;
        Date[] array = new Date[max];
        for (int i = 0; i < max; i++)
            array[i] = randomDate();
        saveArray(array, false, false);
    }

    /*Fix Me aokeke - Takes a lot of time to run */
//    public void testFirstClassObjectArray()throws Exception 
//    {
//        int max = ((int) (Math.random() * 20)) + 5;
//        ArraysTest[] array = new ArraysTest[max];
//        for (int i = 0; i < max; i++)
//            array[i] = new ArraysTest();
//        saveArray(array, false, false);
//    }

    private void saveArray(Object array, boolean primitive, boolean lob)
        throws Exception {
        try {
            saveArrayInternal(array, primitive, lob);
        } catch (Exception e) {
            throw e;
        } catch (Error error) {
            throw error;
        } finally {
            //
        }
    }

    private void saveArrayInternal(Object vals, boolean primitive, boolean lob)
        throws Exception {
        Object[] array;
        if (primitive)
            array = convertPrimitiveArray(vals);
        else
            array = (Object[]) vals;
        Arrays.sort(array, new TestArraySorter());

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);

        ArraysTest test = new ArraysTest();
        pm.persist(test);
        int testID = test.getId();

        setGetTestArray(test, vals, primitive, lob, true);
        endTx(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        ArraysTest retrievedObject = pm.find(ArraysTest.class, testID);

        Object retrievedVals = setGetTestArray(retrievedObject, vals,
            primitive, lob, false);
        Object[] retrievedArray;
        if (primitive)
            retrievedArray = convertPrimitiveArray(retrievedVals);
        else
            retrievedArray = (Object[]) retrievedVals;

        assertNotNull(retrievedArray);
        assertTrue(array.length != 0);
        assertEquals(array.length, retrievedArray.length);
        assertNotNull(array[0]);
        assertNotNull(retrievedArray[0]);

        // make sure the classes of the keys are the same.
        assertEquals(array[0].getClass(), retrievedArray[0].getClass());
        Arrays.sort(retrievedArray, new TestArraySorter());
        for (int i = 0; i < array.length; i++)
            assertClassAndValueEquals(array[i], retrievedArray[i]);

        pm.remove(retrievedObject);
        endTx(pm);
    }

    private Object[] convertPrimitiveArray(Object array) throws Exception {
        int length = Array.getLength(array);
        Class type = Array.get(array, 0).getClass();

        Object[] copy = (Object[]) Array.newInstance(type, length);
        for (int i = 0; i < length; i++)
            copy[i] = Array.get(array, i);

        return copy;
    }

    private void assertClassAndValueEquals(Object o1, Object o2) {
        assertTrue("First object was null", o1 != null);
        assertTrue("Second object was null", o2 != null);

        assertTrue("Types did not match (class1="
            + o1.getClass().getName() + ", class2="
            + o2.getClass().getName() + ")",
            o1.getClass().isAssignableFrom(o2.getClass()));

        // floats and doubles are a little special: we only
        // compare them to a certain precision, after which
        // we give up.
        if (o1 instanceof Double)
            assertEquals(((Double) o1).doubleValue(),
                ((Double) o2).doubleValue(),
                DOUBLE_PRECISION);
        else if (o1 instanceof Float)
            assertEquals(((Float) o1).floatValue(),
                ((Float) o2).floatValue(),
                FLOAT_PRECISION);
        else
            assertEquals("Object did not match (class1="
                + o1.getClass().getName() + ", class2="
                + o2.getClass().getName() + ")",
                o1, o2);
    }

    /**
     * Generic setter/getter for setting the array.
     */
    private Object setGetTestArray(ArraysTest test, Object array,
        boolean primitive, boolean lob, boolean doSet)
        throws Exception {
        if (array == null)
            return null;

        Object first = Array.get(array, 0);
        if (first instanceof Date) {
            if (doSet)
                test.setDate((Date[]) array);
            return test.getDate();
        } else if (first instanceof String) {
            if (doSet)
                test.setString((String[]) array);
            return test.getString();
        } else if (first instanceof Character) {
            if (doSet && !primitive)
                test.setCharacter((Character[]) array);
            else if (doSet && !lob)
                test.setCharacterP((char[]) array);
            else if (doSet)
                test.setCharacterPClob((char[]) array);
            else if (!primitive)
                return test.getCharacter();
            else if (!lob)
                return test.getCharacterP();
            else
                return test.getCharacterPClob();
            return null;
        } else if (first instanceof Double) {
            if (doSet && !primitive)
                test.setDouble((Double[]) array);
            else if (doSet)
                test.setDoubleP((double[]) array);
            else if (!primitive)
                return test.getDouble();
            else
                return test.getDoubleP();
            return null;
        } else if (first instanceof Byte) {
            if (doSet && !primitive)
                test.setByte((Byte[]) array);
            else if (doSet)
                test.setByteP((byte[]) array);
            else if (!primitive)
                return test.getByte();
            else
                return test.getByteP();
            return null;
        } else if (first instanceof Float) {
            if (doSet && !primitive)
                test.setFloat((Float[]) array);
            else if (doSet)
                test.setFloatP((float[]) array);
            else if (!primitive)
                return test.getFloat();
            else
                return test.getFloatP();
            return null;
        } else if (first instanceof Long) {
            if (doSet && !primitive)
                test.setLong((Long[]) array);
            else if (doSet)
                test.setLongP((long[]) array);
            else if (!primitive)
                return test.getLong();
            else
                return test.getLongP();
            return null;
        } else if (first instanceof Integer) {
            if (doSet && !primitive)
                test.setInt((Integer[]) array);
            else if (doSet)
                test.setIntP((int[]) array);
            else if (!primitive)
                return test.getInt();
            else
                return test.getIntP();
            return null;
        } else if (first instanceof Short) {
            if (doSet && !primitive)
                test.setShort((Short[]) array);
            else if (doSet)
                test.setShortP((short[]) array);
            else if (!primitive)
                return test.getShort();
            else
                return test.getShortP();
            return null;
        } else if (first instanceof Boolean) {
            if (doSet && !primitive)
                test.setBoolean((Boolean[]) array);
            else if (doSet)
                test.setBooleanP((boolean[]) array);
            else if (!primitive)
                return test.getBoolean();
            else
                return test.getBooleanP();
            return null;
        } else if (first instanceof ArraysTest) {
            if (doSet)
                test.setArraysTest((ArraysTest[]) array);
            return test.getArraysTest();
        }

        fail("Unknown array type");
        return null;
    }

    private static class TestArraySorter
        implements Comparator {

        private Collator collator = Collator.getInstance();

        public int compare(Object o1, Object o2) {
            if (o1.equals(o2))
                return 0;

            if (o1 instanceof Number) {
                return ((Number) o1).doubleValue() >
                    ((Number) o2).doubleValue() ? 1 : -1;
            } else if (o1 instanceof Date) {
                return ((Date) o1).before((Date) o2) ? 1 : -1;
            } else if (o1 instanceof ArraysTest) {
                return ((ArraysTest) o1).compareTo(o2);
            }
            return collator.compare(o1.toString(), o2.toString());
        }
    }
}

