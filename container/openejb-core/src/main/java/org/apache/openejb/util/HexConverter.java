/*
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
package org.apache.openejb.util;

public class HexConverter {

    private HexConverter() {
        // Disallow instantiation
    }

    /**
     * Converts a byte array to its hexadecimal string representation, ie.
     * new byte[] { 127, 0, 10 } returns "7F000A".
     *
     * @param bytes an array of bytes
     * @return hexadecimal string representation of the first argument
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return buf.toString();
    }

    /**
     * Converts a hexadecimal formatted string into a corresponding byte array.
     *
     * @param hexString a hexadecimal representation of a byte array
     * @return an array of bytes created from the input
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of digits: input must be a string of hexadecimal digit pairs");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

}
