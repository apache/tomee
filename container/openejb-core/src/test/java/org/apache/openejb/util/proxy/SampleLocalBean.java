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
package org.apache.openejb.util.proxy;

import jakarta.ejb.LocalBean;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@LocalBean
public class SampleLocalBean extends BaseLocalBean {

    public SampleLocalBean() {
        super();
    }

    int defaultMethod() {
        return -1;
    }

    @Override
    public String overriddenMethod() {
        return getClass().getName();
    }

    /* 1. void return, no arg */
    public void doWork() {
        System.out.println("void doWork()");
    }

    /* 2. simple object */
    public String echo(final String input) {
        return input;
    }

    /* 3a int primitive */
    public int add(final int no1, final int no2) {
        return no1 + no2;
    }

    /* 3b int object */
    public Integer add(final Integer no1, final Integer no2) {
        return no1 + no2;
    }

    /* 3c bool primitive */
    public boolean isTrue(final boolean value) {
        return value;
    }

    /* 3d bool object */
    public Boolean isTrue(final Boolean value) {
        return value;
    }

    /* 3e char primitive */
    public char nextChar(final char value) {
        if (value > 'z') return 'A';
        char next = value;
        next++;
        return next;
    }

    /* 3f char object */
    public Character nextChar(final Character value) {
        if (value > 'z') return 'A';
        char next = value;
        next++;
        return next;
    }

    /* 3g short primitive */
    public short add(final short no1, final short no2) {
        return (short) (no1 + no2);
    }

    /* 3h short object */
    public Short add(final Short no1, final Short no2) {
        return (short) (no1 + no2);
    }

    /* 3i long primitive */
    public long add(final long no1, final long no2) {
        return (long) (no1 + no2);
    }

    /* 3j long object */
    public Long add(final Long no1, final Long no2) {
        return (long) (no1 + no2);
    }

    /* 3k double primitive */
    public double add(final double no1, final double no2) {
        return (double) (no1 + no2);
    }

    /* 3l double object */
    public Double add(final Double no1, final Double no2) {
        return (Double) (no1 + no2);
    }

    /* 3m float primitive */
    public float add(final float no1, final float no2) {
        return (float) (no1 + no2);
    }

    /* 3n float object*/
    public Float add(final Float no1, final Float no2) {
        return (Float) (no1 + no2);
    }

    /* 4a simple object array */
    public ProxyTestObject[] createSomeObjects() {
        final ProxyTestObject object1 = new ProxyTestObject("object1");
        final ProxyTestObject object2 = new ProxyTestObject("object2");

        return new ProxyTestObject[]{object1, object2};
    }

    /* 4b simple object array */
    public ProxyTestObject returnFirst(final ProxyTestObject[] objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }

        return objects[0];
    }

    /* 5a int primitive */
    public int[] reverse(final int[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final int[] result = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5b int object */
    public Integer[] reverse(final Integer[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Integer[] result = new Integer[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5c bool primitive */
    public boolean[] reverse(final boolean[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final boolean[] result = new boolean[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5d bool object */
    public Boolean[] reverse(final Boolean[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Boolean[] result = new Boolean[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5e char primitive */
    public char[] reverse(final char[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final char[] result = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5f char object */
    public Character[] reverse(final Character[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Character[] result = new Character[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5g short primitive */
    public short[] reverse(final short[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final short[] result = new short[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5h short object */
    public Short[] reverse(final Short[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Short[] result = new Short[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5i long primitive */
    public long[] reverse(final long[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final long[] result = new long[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5j long object */
    public Long[] reverse(final Long[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Long[] result = new Long[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5k double primitive */
    public double[] reverse(final double[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final double[] result = new double[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5l double object */
    public Double[] reverse(final Double[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Double[] result = new Double[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5m float primitive */
    public float[] reverse(final float[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final float[] result = new float[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 5n float object*/
    public Float[] reverse(final Float[] value) {
        if (value == null || value.length == 0) {
            return value;
        }

        final Float[] result = new Float[value.length];
        for (int i = 0; i < value.length; i++) {
            result[(value.length - 1) - i] = value[i];
        }

        return result;
    }

    /* 6 throw and exception */
    public String throwAnException() throws ProxyTestException {
        throw new ProxyTestException();
    }

    public String throwAnotherException() throws ProxyTestException, IOException {
        throw new IOException();
    }

    /* 7 test generics */
    public List<ProxyTestObject> reverseList(final List<ProxyTestObject> objectList) {
        final List<ProxyTestObject> result = new ArrayList<>();
        for (int i = objectList.size() - 1; i >= 0; i--) {
            result.add(objectList.get(i));
        }

        return result;
    }

    /* 8 test multi dimension array */
    public int[][] reverseAll(final int[][] value) {
        final int[][] result = new int[value.length][];
        for (int i = 0; i < value.length; i++) {
            result[i] = reverse(value[(value.length - 1) - i]);
        }

        return result;
    }
}
