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

import junit.framework.TestCase;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.junit.Test;

import jakarta.ejb.EJBException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalBeanProxyFactoryTest extends TestCase {

    public class Call {
        private String methodName;
        private Class<?>[] parameterTypes;

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(final String methodName) {
            this.methodName = methodName;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        public void setParameterTypes(final Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        public Call() {
        }

        public Call(final String methodName, final Class<?>... parameterTypes) {
            this.parameterTypes = parameterTypes;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Call call = (Call) o;

            if (!methodName.equals(call.methodName)) return false;
            if (!Arrays.equals(parameterTypes, call.parameterTypes)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = methodName.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }
    }

    private class TestInvocationHandler implements InvocationHandler {

        private final Object object;
        private final List<Call> calls = new ArrayList<>();

        public TestInvocationHandler(final Object object) {
            super();
            this.object = object;
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final StringBuilder builder = new StringBuilder();
            builder.append(method.getName());
            builder.append("(");

            final Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                final Class<?> parameterType = parameterTypes[i];

                if (i > 0) {
                    builder.append(",");
                }

                builder.append(getObjectType(parameterType));
            }

            builder.append(")");

            System.out.println(builder.toString());

            final Method m = object.getClass().getMethod(method.getName(), method.getParameterTypes());
            calls.add(new Call(m.getName(), m.getParameterTypes()));
            return m.invoke(object, args);
        }

        private String getObjectType(final Class<?> parameterType) {
            String type = "";

            if (parameterType.isPrimitive()) {
                if (Boolean.TYPE.equals(parameterType)) {
                    type = "boolean";
                }

                if (Character.TYPE.equals(parameterType)) {
                    type = "character";
                }

                if (Byte.TYPE.equals(parameterType)) {
                    type = "byte";
                }

                if (Short.TYPE.equals(parameterType)) {
                    type = "short";
                }

                if (Integer.TYPE.equals(parameterType)) {
                    type = "int";
                }

                if (Long.TYPE.equals(parameterType)) {
                    type = "long";
                }

                if (Float.TYPE.equals(parameterType)) {
                    type = "float";
                }

                if (Double.TYPE.equals(parameterType)) {
                    type = "double";
                }

                if (Void.TYPE.equals(parameterType)) {
                    type = "void";
                }

                if (Boolean.TYPE.equals(parameterType)) {
                    type = "boolean";
                }
            } else {
                type = parameterType.getCanonicalName();
            }

            if (parameterType.isArray()) {
                type = type + "[]";
            }

            return type;
        }

        public Call[] getCalls() {
            return calls.toArray(new Call[calls.size()]);
        }
    }

    private SampleLocalBean loadProxy(final TestInvocationHandler invocationHandler) throws Exception {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        return (SampleLocalBean) LocalBeanProxyFactory.newProxyInstance(cl, invocationHandler, SampleLocalBean.class, IntraVmProxy.class, Serializable.class);
    }

    public void testShouldReturnCorrectMethodSignatures() throws Exception {
        final LocalBeanProxyFactory proxyGenerator = new LocalBeanProxyFactory();
        assertEquals("(II)I", proxyGenerator.getMethodSignatureAsString(Integer.TYPE, new Class<?>[]{Integer.TYPE, Integer.TYPE}));
        assertEquals("(ZZ)Z", proxyGenerator.getMethodSignatureAsString(Boolean.TYPE, new Class<?>[]{Boolean.TYPE, Boolean.TYPE}));
        assertEquals("(CC)C", proxyGenerator.getMethodSignatureAsString(Character.TYPE, new Class<?>[]{Character.TYPE, Character.TYPE}));
        assertEquals("(BB)B", proxyGenerator.getMethodSignatureAsString(Byte.TYPE, new Class<?>[]{Byte.TYPE, Byte.TYPE}));
        assertEquals("(SS)S", proxyGenerator.getMethodSignatureAsString(Short.TYPE, new Class<?>[]{Short.TYPE, Short.TYPE}));
        assertEquals("(JJ)J", proxyGenerator.getMethodSignatureAsString(Long.TYPE, new Class<?>[]{Long.TYPE, Long.TYPE}));
        assertEquals("(FF)F", proxyGenerator.getMethodSignatureAsString(Float.TYPE, new Class<?>[]{Float.TYPE, Float.TYPE}));
        assertEquals("(DD)D", proxyGenerator.getMethodSignatureAsString(Double.TYPE, new Class<?>[]{Double.TYPE, Double.TYPE}));
        assertEquals("()V", proxyGenerator.getMethodSignatureAsString(Void.TYPE, new Class<?>[]{}));

        assertEquals("([I[I)[I", proxyGenerator.getMethodSignatureAsString(int[].class, new Class<?>[]{int[].class, int[].class}));
        assertEquals("([Z[Z)[Z", proxyGenerator.getMethodSignatureAsString(boolean[].class, new Class<?>[]{boolean[].class, boolean[].class}));
        assertEquals("([C[C)[C", proxyGenerator.getMethodSignatureAsString(char[].class, new Class<?>[]{char[].class, char[].class}));
        assertEquals("([B[B)[B", proxyGenerator.getMethodSignatureAsString(byte[].class, new Class<?>[]{byte[].class, byte[].class}));
        assertEquals("([S[S)[S", proxyGenerator.getMethodSignatureAsString(short[].class, new Class<?>[]{short[].class, short[].class}));
        assertEquals("([J[J)[J", proxyGenerator.getMethodSignatureAsString(long[].class, new Class<?>[]{long[].class, long[].class}));
        assertEquals("([F[F)[F", proxyGenerator.getMethodSignatureAsString(float[].class, new Class<?>[]{float[].class, float[].class}));
        assertEquals("([D[D)[D", proxyGenerator.getMethodSignatureAsString(double[].class, new Class<?>[]{double[].class, double[].class}));

        assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", proxyGenerator.getMethodSignatureAsString(Integer.class, new Class<?>[]{Integer.class, Integer.class}));
        assertEquals("([Ljava/lang/Integer;[Ljava/lang/Integer;)[Ljava/lang/Integer;", proxyGenerator.getMethodSignatureAsString(Integer[].class, new Class<?>[]{Integer[].class, Integer[].class}));
    }

    @Test
    public void testNonPublicMethods() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);

        try {
            proxy.protectedMethod();
            fail("Protected method did not throw exception");
        } catch (final EJBException e) {
            // that's what we expect
        }

        try {
            proxy.defaultMethod();
            fail("Default method did not throw exception");
        } catch (final EJBException e) {
            // that's what we expect
        }
    }

    @Test
    public void testDoWork() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        proxy.doWork();

        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("doWork", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{}, call.getParameterTypes()));
    }

    @Test
    public void testEcho() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final String result = proxy.echo("Some text");

        assertEquals("Some text", result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("echo", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{String.class}, call.getParameterTypes()));
    }

    @Test
    public void testAddIntInt() throws Exception {
        final int value1 = 32;
        final int value2 = 64;
        final int expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final int result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Integer.TYPE, Integer.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testAddIntegerInteger() throws Exception {
        final Integer value1 = 32;
        final Integer value2 = 64;
        final Integer expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Integer result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Integer.class, Integer.class}, call.getParameterTypes()));
    }

    @Test
    public void testIsTrueBoolean() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final boolean result = proxy.isTrue(Boolean.TRUE);

        assertTrue(result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("isTrue", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Boolean.class}, call.getParameterTypes()));
    }

    @Test
    public void testIsTrueBoolean1() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Boolean result = proxy.isTrue(Boolean.TRUE);

        assertTrue(result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("isTrue", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Boolean.class}, call.getParameterTypes()));
    }

    @Test
    public void testNextCharChar() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final char result = proxy.nextChar(new Character('A').charValue());

        assertEquals('B', result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("nextChar", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Character.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testNextCharCharacter() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Character result = proxy.nextChar(new Character('A'));

        assertEquals(new Character('B'), result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("nextChar", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Character.class}, call.getParameterTypes()));
    }

    @Test
    public void testAddShortShort() throws Exception {
        final short value1 = 32;
        final short value2 = 64;
        final short expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final short result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Short.TYPE, Short.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testAddShortShort1() throws Exception {
        final Short value1 = 32;
        final Short value2 = 64;
        final Short expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Short result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Short.class, Short.class}, call.getParameterTypes()));
    }

    @Test
    public void testAddLongLong() throws Exception {
        final long value1 = 32;
        final long value2 = 64;
        final long expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final long result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Long.TYPE, Long.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testAddLongLong1() throws Exception {
        final Long value1 = 32L;
        final Long value2 = 64L;
        final Long expectedResult = 96L;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Long result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Long.class, Long.class}, call.getParameterTypes()));
    }

    @Test
    public void testAddDoubleDouble() throws Exception {
        final double value1 = 32;
        final double value2 = 64;
        final double expectedResult = 96;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final double result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Double.TYPE, Double.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testAddDoubleDouble1() throws Exception {
        final Double value1 = 32d;
        final Double value2 = 64d;
        final Double expectedResult = 96d;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Double result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Double.class, Double.class}, call.getParameterTypes()));
    }

    @Test
    public void testAddFloatFloat() throws Exception {
        final float value1 = 32f;
        final float value2 = 64f;
        final float expectedResult = 96f;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final float result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Float.TYPE, Float.TYPE}, call.getParameterTypes()));
    }

    @Test
    public void testAddFloatFloat1() throws Exception {
        final Float value1 = 32f;
        final Float value2 = 64f;
        final Float expectedResult = 96f;

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Float result = proxy.add(value1, value2);
        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("add", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Float.class, Float.class}, call.getParameterTypes()));
    }

    @Test
    public void testCreateSomeObjects() throws Exception {
        final ProxyTestObject[] expectedResult = new ProxyTestObject[]{new ProxyTestObject("object1"), new ProxyTestObject("object2")};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final ProxyTestObject[] result = proxy.createSomeObjects();

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("createSomeObjects", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{}, call.getParameterTypes()));
    }

    @Test
    public void testReturnFirst() throws Exception {
        final ProxyTestObject expectedResult = new ProxyTestObject("object1");

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final ProxyTestObject result = proxy.returnFirst(new ProxyTestObject[]{new ProxyTestObject("object1"), new ProxyTestObject("object2")});

        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("returnFirst", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{ProxyTestObject[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseIntArray() throws Exception {
        final int value1 = 2;
        final int value2 = 4;
        final int value3 = 6;
        final int value4 = 8;
        final int value5 = 10;

        final int[] value = new int[]{value1, value2, value3, value4, value5};
        final int[] expectedResult = new int[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final int[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{int[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseIntegerArray() throws Exception {
        final Integer value1 = 2;
        final Integer value2 = 4;
        final Integer value3 = 6;
        final Integer value4 = 8;
        final Integer value5 = 10;

        final Integer[] value = new Integer[]{value1, value2, value3, value4, value5};
        final Integer[] expectedResult = new Integer[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Integer[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Integer[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseBooleanArray() throws Exception {
        final boolean value1 = true;
        final boolean value2 = false;

        final boolean[] value = new boolean[]{value1, value2};
        final boolean[] expectedResult = new boolean[]{value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final boolean[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{boolean[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseBooleanArray1() throws Exception {
        final Boolean value1 = true;
        final Boolean value2 = false;

        final Boolean[] value = new Boolean[]{value1, value2};
        final Boolean[] expectedResult = new Boolean[]{value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Boolean[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Boolean[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseCharArray() throws Exception {
        final char value1 = 'j';
        final char value2 = 'o';
        final char value3 = 'n';

        final char[] value = new char[]{value1, value2, value3};
        final char[] expectedResult = new char[]{value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final char[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{char[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseCharacterArray() throws Exception {
        final Character value1 = 'j';
        final Character value2 = 'o';
        final Character value3 = 'n';

        final Character[] value = new Character[]{value1, value2, value3};
        final Character[] expectedResult = new Character[]{value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Character[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Character[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseShortArray() throws Exception {
        final short value1 = 2;
        final short value2 = 4;
        final short value3 = 6;
        final short value4 = 8;
        final short value5 = 10;

        final short[] value = new short[]{value1, value2, value3, value4, value5};
        final short[] expectedResult = new short[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final short[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{short[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseShortArray1() throws Exception {
        final Short value1 = 2;
        final Short value2 = 4;
        final Short value3 = 6;
        final Short value4 = 8;
        final Short value5 = 10;

        final Short[] value = new Short[]{value1, value2, value3, value4, value5};
        final Short[] expectedResult = new Short[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Short[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Short[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseLongArray() throws Exception {
        final long value1 = 2L;
        final long value2 = 4L;
        final long value3 = 6L;
        final long value4 = 8L;
        final long value5 = 10L;

        final long[] value = new long[]{value1, value2, value3, value4, value5};
        final long[] expectedResult = new long[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final long[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{long[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseLongArray1() throws Exception {
        final Long value1 = 2L;
        final Long value2 = 4L;
        final Long value3 = 6L;
        final Long value4 = 8L;
        final Long value5 = 10L;

        final Long[] value = new Long[]{value1, value2, value3, value4, value5};
        final Long[] expectedResult = new Long[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Long[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Long[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseDoubleArray() throws Exception {
        final double value1 = 2d;
        final double value2 = 4d;
        final double value3 = 6d;
        final double value4 = 8d;
        final double value5 = 10d;

        final double[] value = new double[]{value1, value2, value3, value4, value5};
        final double[] expectedResult = new double[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final double[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{double[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseDoubleArray1() throws Exception {
        final Double value1 = 2d;
        final Double value2 = 4d;
        final Double value3 = 6d;
        final Double value4 = 8d;
        final Double value5 = 10d;

        final Double[] value = new Double[]{value1, value2, value3, value4, value5};
        final Double[] expectedResult = new Double[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Double[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Double[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseFloatArray() throws Exception {
        final float value1 = 2f;
        final float value2 = 4f;
        final float value3 = 6f;
        final float value4 = 8f;
        final float value5 = 10f;

        final float[] value = new float[]{value1, value2, value3, value4, value5};
        final float[] expectedResult = new float[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final float[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{float[].class}, call.getParameterTypes()));
    }

    @Test
    public void testReverseFloatArray1() throws Exception {
        final Float value1 = 2f;
        final Float value2 = 4f;
        final Float value3 = 6f;
        final Float value4 = 8f;
        final Float value5 = 10f;

        final Float[] value = new Float[]{value1, value2, value3, value4, value5};
        final Float[] expectedResult = new Float[]{value5, value4, value3, value2, value1};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final Float[] result = proxy.reverse(value);

        assertTrue(Arrays.equals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverse", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{Float[].class}, call.getParameterTypes()));
    }

    @Test
    public void testThrowAnException() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);

        try {
            proxy.throwAnException();
        } catch (final ProxyTestException e) {
        }

        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("throwAnException", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{}, call.getParameterTypes()));
    }

    @Test
    public void testThrowAnotherException() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);

        try {
            proxy.throwAnotherException();
        } catch (final IOException e) {
        }

        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("throwAnotherException", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{}, call.getParameterTypes()));
    }

    @Test
    public void testGenericCollections() throws Exception {
        final List<ProxyTestObject> value = new ArrayList<>();
        value.add(new ProxyTestObject("test1"));
        value.add(new ProxyTestObject("test2"));

        final List<ProxyTestObject> expectedResult = new ArrayList<>();
        expectedResult.add(new ProxyTestObject("test2"));
        expectedResult.add(new ProxyTestObject("test1"));

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final List<ProxyTestObject> result = proxy.reverseList(value);

        assertEquals(expectedResult, result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverseList", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{List.class}, call.getParameterTypes()));
    }

    @Test
    public void testMultiDimensionArrays() throws Exception {
        final int[][] value = new int[][]{new int[]{1, 2, 3, 4}, new int[]{5, 6, 7, 8}};
        final int[][] expectedResult = new int[][]{new int[]{8, 7, 6, 5}, new int[]{4, 3, 2, 1}};

        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);
        final int[][] result = proxy.reverseAll(value);

        assertTrue(Arrays.deepEquals(expectedResult, result));
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("reverseAll", call.getMethodName());
        assertTrue(Arrays.equals(new Class<?>[]{int[][].class}, call.getParameterTypes()));
    }

    @Test
    public void testAreThoseTwoMethodsTheSame() throws Exception {
        final LocalBeanProxyFactory proxyGenerator = new LocalBeanProxyFactory();

        assertEquals("[I", proxyGenerator.getAsmTypeAsString(int[].class, true));
        assertEquals("[[I", proxyGenerator.getAsmTypeAsString(int[][].class, false));
        assertEquals("Lorg/apache/openejb/util/proxy/ProxyTestObject;", proxyGenerator.getAsmTypeAsString(ProxyTestObject.class, true));
        assertEquals("org/apache/openejb/util/proxy/ProxyTestObject", proxyGenerator.getAsmTypeAsString(ProxyTestObject.class, false));
        assertEquals("[Lorg/apache/openejb/util/proxy/ProxyTestObject;", proxyGenerator.getAsmTypeAsString(ProxyTestObject[].class, true));
        assertEquals("[Lorg/apache/openejb/util/proxy/ProxyTestObject;", proxyGenerator.getAsmTypeAsString(ProxyTestObject[].class, false));

        assertEquals("java/lang/Integer", proxyGenerator.getCastType(Integer.TYPE));
        assertEquals("java/lang/Integer", proxyGenerator.getCastType(Integer.class));
        assertEquals("org/apache/openejb/util/proxy/ProxyTestObject", proxyGenerator.getCastType(ProxyTestObject.class));
    }


    @Test
    public void testEnumParam() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new EnumParams());
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        final LocalBeanProxyFactory generator = new LocalBeanProxyFactory();
        final Class cls = generator.createProxy(EnumParams.class, cl, new Class[]{IntraVmProxy.class, Serializable.class});
        final EnumParams proxy = (EnumParams) generator.constructProxy(cls, invocationHandler);

        proxy.someStringMethod(Color.GREEN.name());
        proxy.someEnumMethod(Color.RED);
        proxy.someInnerClassMethod(new Name(Color.BLUE.name()));

        final Call[] calls = invocationHandler.getCalls();

        assertEquals(3, calls.length);

        assertEquals(new Call("someStringMethod", String.class), calls[0]);
        assertEquals(new Call("someEnumMethod", Color.class), calls[1]);
        assertEquals(new Call("someInnerClassMethod", Name.class), calls[2]);
    }

    public void testGetEnumType() throws Exception {
        System.out.println(Color.class.getCanonicalName());
        final LocalBeanProxyFactory localBeanProxyGenerator = new LocalBeanProxyFactory();
        assertEquals("Lorg/apache/openejb/util/proxy/LocalBeanProxyFactoryTest$Color;", localBeanProxyGenerator.getAsmTypeAsString(Color.class, true));
    }

    @Test
    public void testInheritedMethod() throws Exception {
        final TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
        final SampleLocalBean proxy = loadProxy(invocationHandler);

        // call inherited method
        final String result = proxy.hello("Bob");
        assertEquals("Hello Bob", result);
        assertEquals(1, invocationHandler.getCalls().length);
        final Call call = invocationHandler.getCalls()[0];
        assertEquals("hello", call.getMethodName());

        // call overridden method
        assertEquals(SampleLocalBean.class.getName(), proxy.overriddenMethod());
    }

    public static class EnumParams {

        public void someEnumMethod(final Color s) {
        }

        public void someStringMethod(final String s) {
        }

        public void someInnerClassMethod(final Name s) {
        }
    }

    public static enum Color {
        RED, GREEN, BLUE;
    }

    public static class Name {
        private final String name;

        public Name(final String name) {
            this.name = name;
        }

        public String get() {
            return name;
        }
    }
}
