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
package org.apache.openjpa.lib.util;

/**
 * This class came from the Apache Commons Id sandbox project in support
 * of the UUIDGenerator implementation.
 *
 * <p>Static methods for managing byte arrays (all methods follow Big
 * Endian order where most significant bits are in front).</p>
 */
public final class Bytes {

    /**
     * <p>Hide constructor in utility class.</p>
     */
    private Bytes() {
    }

    /**
     * Appends two bytes array into one.
     *
     * @param a A byte[].
     * @param b A byte[].
     * @return A byte[].
     */
    public static byte[] append(byte[] a, byte[] b) {
        byte[] z = new byte[a.length + b.length];
        System.arraycopy(a, 0, z, 0, a.length);
        System.arraycopy(b, 0, z, a.length, b.length);
        return z;
    }

    /**
     * Returns a 8-byte array built from a long.
     *
     * @param n The number to convert.
     * @return A byte[].
     */
    public static byte[] toBytes(long n) {
        return toBytes(n, new byte[8]);
    }

    /**
     * Build a 8-byte array from a long.  No check is performed on the
     * array length.
     *
     * @param n The number to convert.
     * @param b The array to fill.
     * @return A byte[].
     */
    public static byte[] toBytes(long n, byte[] b) {
        b[7] = (byte) (n);
        n >>>= 8;
        b[6] = (byte) (n);
        n >>>= 8;
        b[5] = (byte) (n);
        n >>>= 8;
        b[4] = (byte) (n);
        n >>>= 8;
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);

        return b;
    }

    /**
     * Build a long from first 8 bytes of the array.
     *
     * @param b The byte[] to convert.
     * @return A long.
     */
    public static long toLong(byte[] b) {
        return ((((long) b[7]) & 0xFF)
                + ((((long) b[6]) & 0xFF) << 8)
                + ((((long) b[5]) & 0xFF) << 16)
                + ((((long) b[4]) & 0xFF) << 24)
                + ((((long) b[3]) & 0xFF) << 32)
                + ((((long) b[2]) & 0xFF) << 40)
                + ((((long) b[1]) & 0xFF) << 48)
                + ((((long) b[0]) & 0xFF) << 56));
    }

    /**
    * Compares two byte arrays for equality.
    *
    * @param a A byte[].
    * @param b A byte[].
    * @return True if the arrays have identical contents.
    */
    public static boolean areEqual(byte[] a, byte[] b) {
        int aLength = a.length;
        if (aLength != b.length) {
            return false;
        }

        for (int i = 0; i < aLength; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Compares two byte arrays as specified by <code>Comparable</code>.
     *
     * @param lhs - left hand value in the comparison operation.
     * @param rhs - right hand value in the comparison operation.
     * @return  a negative integer, zero, or a positive integer as 
     * <code>lhs</code> is less than, equal to, or greater than 
     * <code>rhs</code>.
     */
    public static int compareTo(byte[] lhs, byte[] rhs) {
        if (lhs == rhs) {
            return 0;
        }
        if (lhs == null) {
            return -1;
        }
        if (rhs == null) {
            return +1;
        }
        if (lhs.length != rhs.length) {
            return ((lhs.length < rhs.length) ? -1 : +1);
        }
        for (int i = 0; i < lhs.length; i++) {
            if (lhs[i] < rhs[i]) {
                return -1;
            } else if (lhs[i] > rhs[i]) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Build a short from first 2 bytes of the array.
     *
     * @param b The byte[] to convert.
     * @return A short.
     */
    public static short toShort(byte[] b) {
        return  (short) ((b[1] & 0xFF) + ((b[0] & 0xFF) << 8));
    }
}
