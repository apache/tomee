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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;

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
	public String echo(String input) {
		return input;
	}
	
	/* 3a int primitive */
	public int add(int no1, int no2) {
		return no1 + no2;
	}

	/* 3b int object */
	public Integer add(Integer no1, Integer no2) {
		return no1 + no2;
	}
	
	/* 3c bool primitive */
	public boolean isTrue (boolean value) {
		return value;
	}

	/* 3d bool object */
	public Boolean isTrue (Boolean value) {
		return value;
	}
	
	/* 3e char primitive */
	public char nextChar (char value) {
		if (value > 'z') return 'A';
		char next = value;
		next++;
		return next;
	}
	
	/* 3f char object */
	public Character nextChar (Character value) {
		if (value > 'z') return 'A';
		char next = value;
		next++;
		return next;
	}
	
	/* 3g short primitive */
	public short add(short no1, short no2) {
		return (short) (no1 + no2);
	}
	
	/* 3h short object */
	public Short add(Short no1, Short no2) {
		return new Short((short) (no1 + no2));
	}

	/* 3i long primitive */
	public long add(long no1, long no2) {
		return (long) (no1 + no2);
	}

	/* 3j long object */
	public Long add(Long no1, Long no2) {
		return (long) (no1 + no2);
	}
	
	/* 3k double primitive */
	public double add(double no1, double no2) {
		return (double) (no1 + no2);
	}

	/* 3l double object */
	public Double add(Double no1, Double no2) {
		return (Double) (no1 + no2);
	}
	
	/* 3m float primitive */
	public float add(float no1, float no2) {
		return (float) (no1 + no2);
	}
	
	/* 3n float object*/
	public Float add(Float no1, Float no2) {
		return (Float) (no1 + no2);
	}
	
	/* 4a simple object array */
	public ProxyTestObject[] createSomeObjects() {
		ProxyTestObject object1 = new ProxyTestObject("object1");
		ProxyTestObject object2 = new ProxyTestObject("object2");
		
		return new ProxyTestObject[] { object1, object2 };
	}
	
	/* 4b simple object array */
	public ProxyTestObject returnFirst(ProxyTestObject[] objects) {
		if (objects == null || objects.length == 0) {
			return null;
		}
		
		return objects[0];
	}

	/* 5a int primitive */
	public int[] reverse(int[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		int[] result = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}

	/* 5b int object */
	public Integer[] reverse(Integer[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Integer[] result = new Integer[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5c bool primitive */
	public boolean[] reverse (boolean[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		boolean[] result = new boolean[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}

	/* 5d bool object */
	public Boolean[] reverse (Boolean[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Boolean[] result = new Boolean[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5e char primitive */
	public char[] reverse (char[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		char[] result = new char[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5f char object */
	public Character[] reverse (Character[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Character[] result = new Character[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5g short primitive */
	public short[] reverse(short[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		short[] result = new short[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5h short object */
	public Short[] reverse(Short[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Short[] result = new Short[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}

	/* 5i long primitive */
	public long[] reverse(long[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		long[] result = new long[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}

	/* 5j long object */
	public Long[] reverse(Long[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Long[] result = new Long[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5k double primitive */
	public double[] reverse(double[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		double[] result = new double[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}

	/* 5l double object */
	public Double[] reverse(Double[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Double[] result = new Double[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5m float primitive */
	public float[] reverse(float[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		float[] result = new float[value.length];
		for (int i = 0; i < value.length; i++) {
			result[(value.length - 1) - i] = value[i];
		}
		
		return result;
	}
	
	/* 5n float object*/
	public Float[] reverse(Float[] value) {
		if (value == null || value.length == 0) {
			return value;
		}
		
		Float[] result = new Float[value.length];
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
	public List<ProxyTestObject> reverseList(List<ProxyTestObject> objectList) {
		List<ProxyTestObject> result = new ArrayList<ProxyTestObject>();
		for (int i = objectList.size() - 1; i >= 0; i--) {
			result.add(objectList.get(i));
		}
		
		return result;
	}
	
	/* 8 test multi dimension array */
	public int[][] reverseAll(int[][] value) {
		int[][] result = new int[value.length][];
		for (int i = 0; i < value.length; i++) {
			result[i] = reverse(value[(value.length - 1) - i]);
		}
		
		return result;
	}
}
