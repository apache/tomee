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

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class PojoExternalizationTest extends TestCase {

    public void _testSpeed() throws Exception {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        Green green = new Green(1);
        green.init();

        int count = 20000;

        for (int i = count; i > 0; i--) {
            out.writeObject(new PojoSerialization(green));
        }
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        for (int i = count; i > 0; i--) {
            Green actual = (Green) in.readObject();
            assertEquals(green, actual);
        }
        long finish = System.currentTimeMillis();
        fail("" + (finish - start));
    }

    public void test() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        Green green = new Green(1);
        green.init();

        out.writeObject(new PojoSerialization(green));
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        Green actual = (Green) in.readObject();

        assertEquals(green, actual);
    }

    public static class Color {
        private final double mydouble;
        private final double[] adouble = new double[]{10, 10};
        private float myfloat = 10;
        private final float[] afloat = new float[]{10, 10};
        private long mylong = 10;
        private final long[] along = new long[]{10, 10};
        private int myint = 10;
        private final int[] aint = new int[]{10, 10};
        private short myshort = 10;
        private final short[] ashort = new short[]{10, 10};
        private byte mybyte = 10;
        private final byte[] abyte = new byte[]{10, 10};
        private char mychar = 10;
        private final char[] achar = new char[]{10, 10};
        private boolean myboolean = false;
        private final boolean[] aboolean = new boolean[]{false, false};
        private String myString = "";
        private final String[] aString = new String[]{"one", "two"};
        private final List myList = new ArrayList();
        private final List[] aList = new List[]{new ArrayList(), new ArrayList()};


        public Color() {
            mydouble = 10;
        }

        public Color(int i) {
            mydouble = 20;
        }

        protected void init() {
            adouble[0] = 20;
            adouble[1] = 22;
            myfloat = 20;
            afloat[0] = 20;
            afloat[1] = 22;
            mylong = 20;
            along[0] = 20;
            along[1] = 22;
            myint = 20;
            aint[0] = 20;
            aint[1] = 22;
            myshort = 20;
            ashort[0] = 20;
            ashort[1] = 22;
            mybyte = 20;
            abyte[0] = 20;
            abyte[1] = 22;
            mychar = 20;
            achar[0] = 20;
            achar[1] = 22;
            myboolean = true;
            aboolean[0] = true;
            aboolean[1] = false;
            myString = "hello";
            aString[0] = "three";
            aString[1] = "four";
            aList[0].add("Color.list[0].entryOne");
            aList[0].add("Color.list[0].entryTwo");
            aList[1].add("Color.list[1].entryOne");
            aList[1].add("Color.list[1].entryTwo");
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PojoExternalizationTest.Color color = (PojoExternalizationTest.Color) o;

            if (myboolean != color.myboolean) return false;
            if (mybyte != color.mybyte) return false;
            if (mychar != color.mychar) return false;
            if (Double.compare(color.mydouble, mydouble) != 0) return false;
            if (Float.compare(color.myfloat, myfloat) != 0) return false;
            if (myint != color.myint) return false;
            if (mylong != color.mylong) return false;
            if (myshort != color.myshort) return false;
            if (!Arrays.equals(aList, color.aList)) return false;
            if (!Arrays.equals(aString, color.aString)) return false;
            if (!Arrays.equals(aboolean, color.aboolean)) return false;
            if (!Arrays.equals(abyte, color.abyte)) return false;
            if (!Arrays.equals(achar, color.achar)) return false;
            if (!Arrays.equals(adouble, color.adouble)) return false;
            if (!Arrays.equals(afloat, color.afloat)) return false;
            if (!Arrays.equals(aint, color.aint)) return false;
            if (!Arrays.equals(along, color.along)) return false;
            if (!Arrays.equals(ashort, color.ashort)) return false;
            if (!myList.equals(color.myList)) return false;
            if (!myString.equals(color.myString)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            long temp;
            temp = mydouble != +0.0d ? Double.doubleToLongBits(mydouble) : 0L;
            result = (int) (temp ^ (temp >>> 32));
            result = 29 * result + myfloat != +0.0f ? Float.floatToIntBits(myfloat) : 0;
            result = 29 * result + (int) (mylong ^ (mylong >>> 32));
            result = 29 * result + myint;
            result = 29 * result + (int) myshort;
            result = 29 * result + (int) mybyte;
            result = 29 * result + (int) mychar;
            result = 29 * result + (myboolean ? 1 : 0);
            result = 29 * result + myString.hashCode();
            result = 29 * result + myList.hashCode();
            return result;
        }
    }

    public static class Green extends PojoExternalizationTest.Color {
        private double mydouble = 10;
        private final double[] adouble = new double[]{10, 10};
        private float myfloat = 10;
        private final float[] afloat = new float[]{10, 10};
        private long mylong = 10;
        private final long[] along = new long[]{10, 10};
        private int myint = 10;
        private final int[] aint = new int[]{10, 10};
        private short myshort = 10;
        private final short[] ashort = new short[]{10, 10};
        private byte mybyte = 10;
        private final byte[] abyte = new byte[]{10, 10};
        private char mychar = 10;
        private final char[] achar = new char[]{10, 10};
        private boolean myboolean = false;
        private final boolean[] aboolean = new boolean[]{false, false};
        private String myString = "";
        private final String[] aString = new String[]{"one", "two"};
        private final List myList = new ArrayList();
        private final List[] aList = new List[]{new ArrayList(), new ArrayList()};

        public Green() {
        }

        public Green(int i) {
            super(i);
        }

        protected void init() {
            super.init();
            mydouble = 30;
            adouble[0] = 30;
            adouble[1] = 33;
            myfloat = 30;
            afloat[0] = 30;
            afloat[1] = 33;
            mylong = 30;
            along[0] = 30;
            along[1] = 33;
            myint = 30;
            aint[0] = 30;
            aint[1] = 33;
            myshort = 30;
            ashort[0] = 30;
            ashort[1] = 33;
            mybyte = 30;
            abyte[0] = 30;
            abyte[1] = 33;
            mychar = 30;
            achar[0] = 30;
            achar[1] = 33;
            myboolean = true;
            aboolean[0] = true;
            aboolean[1] = false;
            myString = "hello";
            aString[0] = "three";
            aString[1] = "four";
            aList[0].add("Green.list[0].entryOne");
            aList[0].add("Green.list[0].entryTwo");
            aList[1].add("Green.list[1].entryOne");
            aList[1].add("Green.list[1].entryTwo");
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            final Green green = (Green) o;

            if (myboolean != green.myboolean) return false;
            if (mybyte != green.mybyte) return false;
            if (mychar != green.mychar) return false;
            if (Double.compare(green.mydouble, mydouble) != 0) return false;
            if (Float.compare(green.myfloat, myfloat) != 0) return false;
            if (myint != green.myint) return false;
            if (mylong != green.mylong) return false;
            if (myshort != green.myshort) return false;
            if (!Arrays.equals(aList, green.aList)) return false;
            if (!Arrays.equals(aString, green.aString)) return false;
            if (!Arrays.equals(aboolean, green.aboolean)) return false;
            if (!Arrays.equals(abyte, green.abyte)) return false;
            if (!Arrays.equals(achar, green.achar)) return false;
            if (!Arrays.equals(adouble, green.adouble)) return false;
            if (!Arrays.equals(afloat, green.afloat)) return false;
            if (!Arrays.equals(aint, green.aint)) return false;
            if (!Arrays.equals(along, green.along)) return false;
            if (!Arrays.equals(ashort, green.ashort)) return false;
            if (!myList.equals(green.myList)) return false;
            if (!myString.equals(green.myString)) return false;

            return true;
        }

        public int hashCode() {
            int result = super.hashCode();
            long temp;
            temp = mydouble != +0.0d ? Double.doubleToLongBits(mydouble) : 0L;
            result = 29 * result + (int) (temp ^ (temp >>> 32));
            result = 29 * result + myfloat != +0.0f ? Float.floatToIntBits(myfloat) : 0;
            result = 29 * result + (int) (mylong ^ (mylong >>> 32));
            result = 29 * result + myint;
            result = 29 * result + (int) myshort;
            result = 29 * result + (int) mybyte;
            result = 29 * result + (int) mychar;
            result = 29 * result + (myboolean ? 1 : 0);
            result = 29 * result + myString.hashCode();
            result = 29 * result + myList.hashCode();
            return result;
        }
    }


}
