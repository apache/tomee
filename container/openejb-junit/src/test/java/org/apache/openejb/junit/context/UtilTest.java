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
package org.apache.openejb.junit.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * @author quintin
 */
public class UtilTest {
    /*
    * No setter method
    */
    private String noSetter;

    /**
     * 0 parameter setter
     */
    private String noParamSetter;

    public void setNoParamSetter() {
    }

    /**
     * Setter with multiple parameter
     */
    private String multiParamSetter;

    public void setMultiParamSetter(String s1, String s2) {
    }

    /**
     * Setter isn't void
     */
    private String notVoidSetter;

    public String setNotVoidSetter(String s) {
        return null;
    }

    /**
     * Setter has a primitive argument, but is supplied a null argument
     */
    private int primitiveParamSetter;

    public void setPrimitiveParamSetter(int i) {
    }

    /**
     * Setter has a different type of parameter
     */
    private String wrongInstanceSetter;

    public void setWrongInstanceSetter(Integer i) {
    }

    /**
     * A static setter with an instance field, which can't be a valid setter.
     */
    private String staticSetterInstanceField;

    private static void setStaticSetterInstanceField(String s) {
    }

    private static abstract class AbstractClass {
        private String abstractSetter;

        abstract void setAbstractSetter(String s);
    }

    /*
    * Setter is private, so it should be setAccessible
    */
    private String privateSetter;

    private void setPrivateSetter(String s) {
    }

    /*
    * Static setter and field
    */
    private static String staticSetterStaticField;

    public static void setStaticSetterStaticField(String s) {
    }

    /**
     * Instance setter for a static field
     */
    private static String instanceSetterStaticField;

    public void setInstanceSetterStaticField(String s) {
    }

    /**
     * Instance field
     */
    private String instanceField;

    public void setInstanceField(String s) {
    }

    /**
     * Primitive instance field and setter
     */
    private int primitiveInstanceField;

    public void setPrimitiveInstanceField(int i) {
    }

    /*
    * Two name matching setters, of which only one should be found
    */
    private String twoSetter;

    public void setTwoSetter(String s, String s1) {
    }

    public void setTwoSetter(String s) {
    }

    /**
     * 2 matching setters, should pick the most specific, ie. String
     */
    private String twoAccessibleSetter;

    public void setTwoAccessibleSetter(String s) {
    }

    public void setTwoAccessibleSetter(Object s) {
    }

    /**
     * 2 matching setters, should pick the most specific, ie. IllegalArgumentException
     */
    private IllegalArgumentException threeAccessibleNoExactMatchSetter;

    public void setThreeAccessibleNoExactMatchSetter(Object s) {
    }

    public void setThreeAccessibleNoExactMatchSetter(Throwable s) {
    }

    public void setThreeAccessibleNoExactMatchSetter(Exception s) {
    }


    /**
     * Used to test failure of finding a setter, where a method with a name equal
     * to the setter name exists. This would be cases where the method doesn't match
     * a method
     */
    private void findInvalidSetter(String fieldName, Object setValue) throws Exception {
        // check the field exists
        Field field = getClass().getDeclaredField(fieldName);

        // check a method with the correct name exists
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        boolean foundMethod = false;
        for (Method method : getClass().getDeclaredMethods()) {
            if (setterName.equals(method.getName())) {
                foundMethod = true;
                break;
            }
        }
        assertTrue("No Method exists with name: " + setterName, foundMethod);

        // now check that the findSetter method returns null
        Method ret = Util.findSetter(getClass(), field, setValue);
        assertNull("Invalid setter returned.", ret);
    }

    /**
     * Used to test success of finding a setter, where a method with a name equal
     * to the setter name exists and findSetter returns that method.
     *
     * @return found setter so further checks can be performed
     */
    private Method findValidSetter(String fieldName, Object setValue) throws Exception {
        // check the field exists
        Field field = getClass().getDeclaredField(fieldName);

        // check a method with the correct name exists
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method foundMethod = null;
        for (Method method : getClass().getDeclaredMethods()) {
            if (setterName.equals(method.getName())) {
                foundMethod = method;
                break;
            }
        }
        assertNotNull("No Method exists with name: " + setterName, foundMethod);

        // now check that the findSetter method returns the desired valid method
        Method ret = Util.findSetter(getClass(), field, setValue);
        assertNotNull("Couldn't find setter.", ret);
        assertEquals(foundMethod, ret);

        return ret;
    }

    @Test
    public void testFindSetterInvalidSetters() throws Exception {
        findInvalidSetter("noParamSetter", "ValueString");
        findInvalidSetter("multiParamSetter", "ValueString");
        findInvalidSetter("notVoidSetter", "ValueString");
        findInvalidSetter("primitiveParamSetter", null);
        findInvalidSetter("wrongInstanceSetter", "ValueString");
        findInvalidSetter("staticSetterInstanceField", "ValueString");
    }

    @Test
    public void testFindSetterNoSetters() throws Exception {
        String fieldName = "noSetter";
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        Field field = getClass().getDeclaredField(fieldName);

        try {
            getClass().getDeclaredMethod(setterName, String.class);
        }
        catch (NoSuchMethodException e) {
        }

        Method method = Util.findSetter(getClass(), field, "ValueString");
        assertNull("Setter found.", method);
    }

    @Test
    public void testFindSetterAbstractSetter() throws Exception {
        String fieldName = "abstractSetter";
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        Field field = AbstractClass.class.getDeclaredField(fieldName);

        // will fail if the method doesn't exist
        AbstractClass.class.getDeclaredMethod(setterName, String.class);

        Method method = Util.findSetter(AbstractClass.class, field, "ValueString");
        assertNull("Abstract setter returned.", method);
    }

    @Test
    public void testFindSetterValidSetters() throws Exception {
        findValidSetter("staticSetterStaticField", "ValueString");
        findValidSetter("instanceSetterStaticField", "ValueString");
        findValidSetter("instanceField", "ValueString");
        findValidSetter("primitiveInstanceField", 1);

        Method privateMethod = findValidSetter("privateSetter", "ValueString");
        assertTrue(privateMethod.isAccessible());
    }

    @Test
    public void testFindSetterDualValidSetters() throws Exception {
        String fieldName = "twoSetter";

        // check the field exists
        Field field = getClass().getDeclaredField(fieldName);

        // check a method with the correct name exists
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        getClass().getDeclaredMethod(setterName, String.class, String.class);
        Method setterMethod = getClass().getDeclaredMethod(setterName, String.class);

        // now check that the findSetter method returns null
        Method ret = Util.findSetter(getClass(), field, "ValueString");
        assertNotNull("Couldn't find setter.", ret);
        assertEquals(setterMethod, ret);
    }

    @Test
    public void testFindSetterBestMatchSetters() throws Exception {
        String fieldName = "twoAccessibleSetter";

        // check the field exists
        Field field = getClass().getDeclaredField(fieldName);

        // check a method with the correct name exists
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        getClass().getDeclaredMethod(setterName, Object.class);
        Method setterMethod = getClass().getDeclaredMethod(setterName, String.class);

        // now check that the findSetter method returns null
        Method ret = Util.findSetter(getClass(), field, "ValueString");
        assertNotNull("Couldn't find setter.", ret);
        assertEquals(setterMethod, ret);
    }

    @Test
    public void testFindSetterThreeAccessibleNoExactMatchSetter() throws Exception {
        String fieldName = "threeAccessibleNoExactMatchSetter";

        // check the field exists
        Field field = getClass().getDeclaredField(fieldName);

        // check a method with the correct name exists
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        getClass().getDeclaredMethod(setterName, Object.class);
        getClass().getDeclaredMethod(setterName, Throwable.class);
        Method setterMethod = getClass().getDeclaredMethod(setterName, Exception.class);

        // now check that the findSetter method returns null
        Method ret = Util.findSetter(getClass(), field, new NumberFormatException());
        assertNotNull("Couldn't find setter.", ret);
        assertEquals(setterMethod, ret);
    }

    @Test
    public void testAddProperty() {
        checkAddProperty("name=value", "name", "value");
        checkAddProperty("name=", "name", "");
        checkAddProperty("name", "name", "");
        checkAddProperty(" name=value ", "name", "value");
        checkAddProperty(" name = value ", "name", "value");
        checkAddProperty("name = value", "name", "value");
        checkAddProperty(" name= ", "name", "");
        checkAddProperty(" name = ", "name", "");
        checkAddProperty("name =", "name", "");
        checkAddProperty("name", "name", "");
        checkAddProperty(" name", "name", "");
        checkAddProperty(" name ", "name", "");
        failAddProperty("=", IllegalArgumentException.class);
        failAddProperty("=value", IllegalArgumentException.class);
        failAddProperty(null, IllegalArgumentException.class);
        failAddProperty("", IllegalArgumentException.class);
    }

    /**
     * Checks if the addProperty method works, adding a property with the specified
     * key/value
     *
     * @param property
     * @param key
     * @param value
     */
    private void checkAddProperty(String property, String key, String value) {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        Util.addProperty(hashtable, property);

        assertEquals(1, hashtable.size());
        assertNotNull(hashtable.get(key));
        assertEquals(value, hashtable.get(key));
    }

    /**
     * Runs the addProperty and fails if it succeeds
     *
     * @param property
     * @param exceptionType the type of failure that should occur
     */
    private void failAddProperty(String property, Class<?> exceptionType) {
        try {
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            Util.addProperty(hashtable, property);
            fail("Add property succeeded where it shouldn't have.");
        }
        catch (Exception e) {
            assertEquals(exceptionType, e.getClass());
        }
    }

    /**
     * Test for isInstance()
     */
    @Test
    public void testIsInstance() {
        // check non primitives first
        checkIsInstance(true, Object.class, null);
        checkIsInstance(true, Object.class, "String");
        checkIsInstance(true, String.class, "String");
        checkIsInstance(false, Integer.class, "String");

        // null primitive
        checkIsInstance(false, int.class, null);

        // then primitives.
        // We check each for a match, and then both a non-match against a primitive and object
        checkIsInstancePrimitive(boolean.class, true);
        checkIsInstancePrimitive(char.class, 'a');
        checkIsInstancePrimitive(byte.class, (byte) 1);
        checkIsInstancePrimitive(short.class, (short) 1);
        checkIsInstancePrimitive(int.class, 1);
        checkIsInstancePrimitive(long.class, 1L);
        checkIsInstancePrimitive(float.class, 1.0f);
        checkIsInstancePrimitive(double.class, 1.0);
    }

    /**
     * Runs a check against isInstance for a primitive type. It does 3 checks
     * <ol>
     * <li>A valid instance
     * <li>An invalid primitive instance
     * <li>An invalid non-primitive instance
     * <ol>
     *
     * @param type
     * @param validInstance
     */
    private void checkIsInstancePrimitive(Class<?> type, Object validInstance) {
        // the invalidPrimitive is an integer in all cases, except against class int.class
        // where we use a long
        Object invalidPrimitive;
        if (type == int.class) {
            invalidPrimitive = 1L;
        } else {
            invalidPrimitive = 1;
        }

        checkIsInstance(true, type, validInstance);
        checkIsInstance(false, type, invalidPrimitive);
        checkIsInstance(false, type, "Object");
    }

    /**
     * Runs a check against isInstance, checking if it returns the expected value
     *
     * @param type
     * @param instance
     * @param expected Expected return value
     */
    private void checkIsInstance(boolean expected, Class<?> type, Object instance) {
        assertEquals(expected, Util.isInstance(type, instance));
    }

    /**
     * Tests the most specific method test
     */
    @Test
    public void testGetMostSpecificMethod() throws Exception {
        Method mObject = getClass().getDeclaredMethod("setThreeAccessibleNoExactMatchSetter", Object.class);
        Method mThrowable = getClass().getDeclaredMethod("setThreeAccessibleNoExactMatchSetter", Throwable.class);
        Method mException = getClass().getDeclaredMethod("setThreeAccessibleNoExactMatchSetter", Exception.class);

        assertEquals(mThrowable, Util.getMostSpecificMethod(mObject, mThrowable));
        assertEquals(mException, Util.getMostSpecificMethod(mException, mThrowable));
        assertEquals(mException, Util.getMostSpecificMethod(mException, mObject));
    }
}
