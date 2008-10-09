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
