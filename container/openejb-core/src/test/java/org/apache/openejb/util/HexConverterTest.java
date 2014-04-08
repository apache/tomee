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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class HexConverterTest {

	@Test
	public void testBytesToHex() {
		byte[] bytes = new byte[] { 0, 8, 10, 127, 50 };
		String hexString = HexConverter.bytesToHex(bytes);
		assertEquals("00080A7F32", hexString);
	}

	@Test
	public void testStringToBytes() {
		final byte[] correctAnswer = new byte[] { (byte) 166, (byte) 253, 0, 18, (byte) 184};
		String hexString = "A6FD0012B8";
		byte[] bytes = HexConverter.hexToBytes(hexString);
		assertEquals(bytes.length, correctAnswer.length);
		for (int i = 0; i < bytes.length; i++)
			assertEquals(correctAnswer[i], bytes[i]);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testWrongNumberOfDigits() {
		String hexString = "F5D";
		HexConverter.hexToBytes(hexString);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHexString() {
		String hexString = "A9G0";
		HexConverter.hexToBytes(hexString);
	}

}
