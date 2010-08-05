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
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalBeanProxyGeneratorImplTest extends TestCase {
	
	public class Call {
		private String methodName;
		private Class<?>[] parameterTypes;

        public String getMethodName() {
			return methodName;
		}
		
		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
		
		public Class<?>[] getParameterTypes() {
			return parameterTypes;
		}
		
		public void setParameterTypes(Class<?>[] parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		public Call() {
		}

		public Call(String methodName, Class<?>... parameterTypes) {
			this.parameterTypes = parameterTypes;
			this.methodName = methodName;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Call call = (Call) o;

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
		private List<Call> calls = new ArrayList<Call>();

		public TestInvocationHandler(Object object) {
			super();
			this.object = object;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			StringBuilder builder = new StringBuilder();
			builder.append(method.getName());
			builder.append("(");
			
			Class<?>[] parameterTypes = method.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Class<?> parameterType = parameterTypes[i];
				
				if (i > 0) {
					builder.append(",");
				}
				
				builder.append(getObjectType(parameterType));				
			}
			
			builder.append(")");
			
			System.out.println(builder.toString());
			
			Method m = object.getClass().getMethod(method.getName(), method.getParameterTypes());
			calls.add(new Call(m.getName(), m.getParameterTypes()));
			return m.invoke(object, args);
		}

		private String getObjectType(Class<?> parameterType) {
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

	private SampleLocalBean loadProxy(TestInvocationHandler invocationHandler) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Class cls = new LocalBeanProxyGeneratorImpl().createProxy(SampleLocalBean.class, cl);
        return (SampleLocalBean) cls.getConstructor(new Class[] { InvocationHandler.class }).newInstance(invocationHandler);
	}

	public void testShouldReturnCorrectMethodSignatures() throws Exception {
		LocalBeanProxyGeneratorImpl proxyGenerator = new LocalBeanProxyGeneratorImpl();
		assertEquals("(II)I", proxyGenerator.getMethodSignatureAsString(Integer.TYPE, new Class<?>[] { Integer.TYPE, Integer.TYPE }));
		assertEquals("(ZZ)Z", proxyGenerator.getMethodSignatureAsString(Boolean.TYPE, new Class<?>[] { Boolean.TYPE, Boolean.TYPE }));
		assertEquals("(CC)C", proxyGenerator.getMethodSignatureAsString(Character.TYPE, new Class<?>[] { Character.TYPE, Character.TYPE }));		
		assertEquals("(BB)B", proxyGenerator.getMethodSignatureAsString(Byte.TYPE, new Class<?>[] { Byte.TYPE, Byte.TYPE }));		
		assertEquals("(SS)S", proxyGenerator.getMethodSignatureAsString(Short.TYPE, new Class<?>[] { Short.TYPE, Short.TYPE }));		
		assertEquals("(JJ)J", proxyGenerator.getMethodSignatureAsString(Long.TYPE, new Class<?>[] { Long.TYPE, Long.TYPE }));		
		assertEquals("(FF)F", proxyGenerator.getMethodSignatureAsString(Float.TYPE, new Class<?>[] { Float.TYPE, Float.TYPE }));		
		assertEquals("(DD)D", proxyGenerator.getMethodSignatureAsString(Double.TYPE, new Class<?>[] { Double.TYPE, Double.TYPE }));		
		assertEquals("()V", proxyGenerator.getMethodSignatureAsString(Void.TYPE, new Class<?>[] { }));		

		assertEquals("([I[I)[I", proxyGenerator.getMethodSignatureAsString(int[].class, new Class<?>[] { int[].class, int[].class }));
		assertEquals("([Z[Z)[Z", proxyGenerator.getMethodSignatureAsString(boolean[].class, new Class<?>[] { boolean[].class, boolean[].class }));
		assertEquals("([C[C)[C", proxyGenerator.getMethodSignatureAsString(char[].class, new Class<?>[] { char[].class, char[].class }));		
		assertEquals("([B[B)[B", proxyGenerator.getMethodSignatureAsString(byte[].class, new Class<?>[] { byte[].class, byte[].class }));		
		assertEquals("([S[S)[S", proxyGenerator.getMethodSignatureAsString(short[].class, new Class<?>[] { short[].class, short[].class }));		
		assertEquals("([J[J)[J", proxyGenerator.getMethodSignatureAsString(long[].class, new Class<?>[] { long[].class, long[].class }));		
		assertEquals("([F[F)[F", proxyGenerator.getMethodSignatureAsString(float[].class, new Class<?>[] { float[].class, float[].class }));		
		assertEquals("([D[D)[D", proxyGenerator.getMethodSignatureAsString(double[].class, new Class<?>[] { double[].class, double[].class }));		
		
		assertEquals("(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;", proxyGenerator.getMethodSignatureAsString(Integer.class, new Class<?>[] { Integer.class, Integer.class }));
		assertEquals("([Ljava/lang/Integer;[Ljava/lang/Integer;)[Ljava/lang/Integer;", proxyGenerator.getMethodSignatureAsString(Integer[].class, new Class<?>[] { Integer[].class, Integer[].class }));
	}
	
	@Test
	public void testDoWork() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		proxy.doWork();
		
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("doWork", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { }, call.getParameterTypes()));
	}

	@Test
	public void testEcho() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		String result = proxy.echo("Some text");

		assertEquals("Some text", result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("echo", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { String.class }, call.getParameterTypes()));
	}

	@Test
	public void testAddIntInt() throws Exception {
		int value1 = 32;
		int value2 = 64;
		int expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		int result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Integer.TYPE, Integer.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testAddIntegerInteger() throws Exception {
		Integer value1 = 32;
		Integer value2 = 64;
		Integer expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Integer result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Integer.class, Integer.class }, call.getParameterTypes()));
	}

	@Test
	public void testIsTrueBoolean() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		boolean result = proxy.isTrue(new Boolean(true).booleanValue());

		assertTrue(result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("isTrue", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Boolean.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testIsTrueBoolean1() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Boolean result = proxy.isTrue(new Boolean(true));

		assertTrue(result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("isTrue", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Boolean.class }, call.getParameterTypes()));
	}

	@Test
	public void testNextCharChar() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		char result = proxy.nextChar(new Character('A').charValue());

		assertEquals('B', result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("nextChar", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Character.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testNextCharCharacter() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Character result = proxy.nextChar(new Character('A'));

		assertEquals(new Character('B'), result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("nextChar", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Character.class }, call.getParameterTypes()));
	}

	@Test
	public void testAddShortShort() throws Exception {
		short value1 = 32;
		short value2 = 64;
		short expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		short result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Short.TYPE, Short.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testAddShortShort1() throws Exception {
		Short value1 = 32;
		Short value2 = 64;
		Short expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Short result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Short.class, Short.class }, call.getParameterTypes()));
	}

	@Test
	public void testAddLongLong() throws Exception {
		long value1 = 32;
		long value2 = 64;
		long expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		long result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Long.TYPE, Long.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testAddLongLong1() throws Exception {
		Long value1 = 32L;
		Long value2 = 64L;
		Long expectedResult = 96L;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Long result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Long.class, Long.class }, call.getParameterTypes()));
	}

	@Test
	public void testAddDoubleDouble() throws Exception {
		double value1 = 32;
		double value2 = 64;
		double expectedResult = 96;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		double result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Double.TYPE, Double.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testAddDoubleDouble1() throws Exception {
		Double value1 = 32d;
		Double value2 = 64d;
		Double expectedResult = 96d;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Double result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Double.class, Double.class }, call.getParameterTypes()));
	}

	@Test
	public void testAddFloatFloat() throws Exception {
		float value1 = 32f;
		float value2 = 64f;
		float expectedResult = 96f;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		float result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Float.TYPE, Float.TYPE }, call.getParameterTypes()));
	}

	@Test
	public void testAddFloatFloat1() throws Exception {
		Float value1 = 32f;
		Float value2 = 64f;
		Float expectedResult = 96f;
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Float result = proxy.add(value1, value2);
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("add", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Float.class, Float.class }, call.getParameterTypes()));
	}

	@Test
	public void testCreateSomeObjects() throws Exception {
		ProxyTestObject[] expectedResult = new ProxyTestObject[] { new ProxyTestObject("object1"), new ProxyTestObject("object2") };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		ProxyTestObject[] result = proxy.createSomeObjects();
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("createSomeObjects", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { }, call.getParameterTypes()));
	}

	@Test
	public void testReturnFirst() throws Exception {
		ProxyTestObject expectedResult = new ProxyTestObject("object1");
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		ProxyTestObject result = proxy.returnFirst(new ProxyTestObject[] { new ProxyTestObject("object1"), new ProxyTestObject("object2") });
		
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("returnFirst", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { ProxyTestObject[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseIntArray() throws Exception {
		int value1 = 2;
		int value2 = 4;
		int value3 = 6;
		int value4 = 8;
		int value5 = 10;
		
		int[] value = new int[] { value1, value2, value3, value4, value5 };
		int[] expectedResult = new int[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		int[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { int[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseIntegerArray() throws Exception {
		Integer value1 = 2;
		Integer value2 = 4;
		Integer value3 = 6;
		Integer value4 = 8;
		Integer value5 = 10;
		
		Integer[] value = new Integer[] { value1, value2, value3, value4, value5 };
		Integer[] expectedResult = new Integer[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Integer[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Integer[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseBooleanArray() throws Exception {
		boolean value1 = true;
		boolean value2 = false;
		
		boolean[] value = new boolean[] { value1, value2 };
		boolean[] expectedResult = new boolean[] { value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		boolean[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { boolean[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseBooleanArray1() throws Exception {
		Boolean value1 = true;
		Boolean value2 = false;
		
		Boolean[] value = new Boolean[] { value1, value2 };
		Boolean[] expectedResult = new Boolean[] { value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Boolean[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Boolean[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseCharArray() throws Exception {
		char value1 = 'j';
		char value2 = 'o';
		char value3 = 'n';
		
		char[] value = new char[] { value1, value2, value3 };
		char[] expectedResult = new char[] { value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		char[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { char[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseCharacterArray() throws Exception {
		Character value1 = 'j';
		Character value2 = 'o';
		Character value3 = 'n';
		
		Character[] value = new Character[] { value1, value2, value3 };
		Character[] expectedResult = new Character[] { value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Character[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Character[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseShortArray() throws Exception {
		short value1 = 2;
		short value2 = 4;
		short value3 = 6;
		short value4 = 8;
		short value5 = 10;
		
		short[] value = new short[] { value1, value2, value3, value4, value5 };
		short[] expectedResult = new short[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		short[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { short[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseShortArray1() throws Exception {
		Short value1 = 2;
		Short value2 = 4;
		Short value3 = 6;
		Short value4 = 8;
		Short value5 = 10;
		
		Short[] value = new Short[] { value1, value2, value3, value4, value5 };
		Short[] expectedResult = new Short[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Short[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Short[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseLongArray() throws Exception {
		long value1 = 2L;
		long value2 = 4L;
		long value3 = 6L;
		long value4 = 8L;
		long value5 = 10L;
		
		long[] value = new long[] { value1, value2, value3, value4, value5 };
		long[] expectedResult = new long[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		long[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { long[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseLongArray1() throws Exception {
		Long value1 = 2L;
		Long value2 = 4L;
		Long value3 = 6L;
		Long value4 = 8L;
		Long value5 = 10L;
		
		Long[] value = new Long[] { value1, value2, value3, value4, value5 };
		Long[] expectedResult = new Long[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Long[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Long[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseDoubleArray() throws Exception {
		double value1 = 2d;
		double value2 = 4d;
		double value3 = 6d;
		double value4 = 8d;
		double value5 = 10d;
		
		double[] value = new double[] { value1, value2, value3, value4, value5 };
		double[] expectedResult = new double[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		double[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { double[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseDoubleArray1() throws Exception {
		Double value1 = 2d;
		Double value2 = 4d;
		Double value3 = 6d;
		Double value4 = 8d;
		Double value5 = 10d;
		
		Double[] value = new Double[] { value1, value2, value3, value4, value5 };
		Double[] expectedResult = new Double[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Double[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Double[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseFloatArray() throws Exception {
		float value1 = 2f;
		float value2 = 4f;
		float value3 = 6f;
		float value4 = 8f;
		float value5 = 10f;
		
		float[] value = new float[] { value1, value2, value3, value4, value5 };
		float[] expectedResult = new float[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		float[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { float[].class }, call.getParameterTypes()));
	}

	@Test
	public void testReverseFloatArray1() throws Exception {
		Float value1 = 2f;
		Float value2 = 4f;
		Float value3 = 6f;
		Float value4 = 8f;
		Float value5 = 10f;
		
		Float[] value = new Float[] { value1, value2, value3, value4, value5 };
		Float[] expectedResult = new Float[] { value5, value4, value3, value2, value1 };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		Float[] result = proxy.reverse(value);
		
		assertTrue(Arrays.equals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverse", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { Float[].class }, call.getParameterTypes()));
	}

	@Test
	public void testThrowAnException() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);

		try {
			proxy.throwAnException();
		} catch (ProxyTestException e) {
		}
		
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("throwAnException", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { }, call.getParameterTypes()));
	}
	
	@Test
	public void testThrowAnotherException() throws Exception {
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);

		try {
			proxy.throwAnotherException();
		} catch (IOException e) {
		}
		
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("throwAnotherException", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { }, call.getParameterTypes()));
	}
	
	@Test
	public void testGenericCollections() throws Exception {
		List<ProxyTestObject> value = new ArrayList<ProxyTestObject>();
		value.add(new ProxyTestObject("test1"));
		value.add(new ProxyTestObject("test2"));

		List<ProxyTestObject> expectedResult = new ArrayList<ProxyTestObject>();
		expectedResult.add(new ProxyTestObject("test2"));
		expectedResult.add(new ProxyTestObject("test1"));
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		List<ProxyTestObject> result = proxy.reverseList(value);
		
		assertEquals(expectedResult, result);
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverseList", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { List.class }, call.getParameterTypes()));
	}

	@Test
	public void testMultiDimensionArrays() throws Exception {
		int[][] value = new int[][] { new int[] { 1, 2, 3, 4 }, new int[] { 5, 6, 7, 8 } };
		int[][] expectedResult = new int[][] { new int[] { 8, 7, 6, 5 }, new int[] { 4, 3, 2, 1 } };
		
		TestInvocationHandler invocationHandler = new TestInvocationHandler(new SampleLocalBean());
		SampleLocalBean proxy = loadProxy(invocationHandler);
		int[][] result = proxy.reverseAll(value);
		
		assertTrue(Arrays.deepEquals(expectedResult, result));
		assertEquals(1, invocationHandler.getCalls().length);
		Call call = invocationHandler.getCalls()[0];
		assertEquals("reverseAll", call.getMethodName());
		assertTrue(Arrays.equals(new Class<?>[] { int[][].class }, call.getParameterTypes()));
	}
	
	@Test
	public void testAreThoseTwoMethodsTheSame() throws Exception {
		LocalBeanProxyGeneratorImpl proxyGenerator = new LocalBeanProxyGeneratorImpl();
		
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
        TestInvocationHandler invocationHandler = new TestInvocationHandler(new EnumParams());
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Class cls = new LocalBeanProxyGeneratorImpl().createProxy(EnumParams.class, cl);
        EnumParams proxy = (EnumParams) cls.getConstructor(new Class[] { InvocationHandler.class }).newInstance(invocationHandler);

        proxy.someStringMethod(Color.GREEN.name());
        proxy.someEnumMethod(Color.RED);
        proxy.someInnerClassMethod(new Name(Color.BLUE.name()));

        Call[] calls = invocationHandler.getCalls();

        assertEquals(3, calls.length);

        assertEquals(new Call("someStringMethod", String.class), calls[0]);
        assertEquals(new Call("someEnumMethod", Color.class), calls[1]);
        assertEquals(new Call("someInnerClassMethod", Name.class), calls[2]);
    }

    public void testGetEnumType() throws Exception {
        System.out.println(Color.class.getCanonicalName());
        LocalBeanProxyGeneratorImpl localBeanProxyGenerator = new LocalBeanProxyGeneratorImpl();
        assertEquals("Lorg/apache/openejb/util/proxy/LocalBeanProxyGeneratorImplTest$Color;", localBeanProxyGenerator.getAsmTypeAsString(Color.class, true));
    }


    public static class EnumParams {

        public void someEnumMethod(Color s){
        }

        public void someStringMethod(String s){
        }

        public void someInnerClassMethod(Name s){
        }
    }

    public static enum Color {
        RED, GREEN, BLUE;
    }

    public static class Name {
        private final String name;

        public Name(String name) {
            this.name = name;
        }

        public String get() {
            return name;
        }
    }
}
