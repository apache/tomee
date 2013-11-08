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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;

/**
 * Used in testing; should be enhanced.
 */
@Entity
public class ArraysTest
    implements Comparable {

    private static long counter = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @PersistentCollection
    private String[] aString;

    @PersistentCollection
    private Date[] aDate;

    @PersistentCollection
    private Integer[] aInt;

    @PersistentCollection
    private Long[] aLong;

    @PersistentCollection

    private Float[] aFloat;

    private Byte[] aByte;

    @PersistentCollection
    private Double[] aDouble;

    @PersistentCollection
    private Boolean[] aBoolean;

    @PersistentCollection
    private Short[] aShort;

    private Character[] aCharacter;

    @PersistentCollection
    private int[] aIntP;

    @PersistentCollection
    private long[] aLongP;

    @PersistentCollection
    private float[] aFloatP;

    private byte[] aByteP;

    @PersistentCollection
    private double[] aDoubleP;

    @PersistentCollection
    private boolean[] aBooleanP;

    @PersistentCollection
    private short[] aShortP;

    private char[] aCharacterP;

    private char[] aCharacterPClob;

    @PersistentCollection
    private ArraysTest[] aArraysTest;

    public ArraysTest() {
    }

    public boolean equals(Object other) {
        return (other instanceof ArraysTest) && id == ((ArraysTest) other).id;
    }

    public int compareTo(Object other) {
        ArraysTest t = (ArraysTest) other;
        if (id < t.id)
            return -1;
        if (id == t.id)
            return 0;
        return 1;
    }

    public int getId() {
        return id;
    }

    public String[] getString() {
        return aString;
    }

    public Date[] getDate() {
        return aDate;
    }

    public Integer[] getInt() {
        return aInt;
    }

    public Long[] getLong() {
        return aLong;
    }

    public Float[] getFloat() {
        return aFloat;
    }

    public Byte[] getByte() {
        return aByte;
    }

    public Double[] getDouble() {
        return aDouble;
    }

    public Boolean[] getBoolean() {
        return aBoolean;
    }

    public Short[] getShort() {
        return aShort;
    }

    public Character[] getCharacter() {
        return aCharacter;
    }

    public int[] getIntP() {
        return aIntP;
    }

    public long[] getLongP() {
        return aLongP;
    }

    public float[] getFloatP() {
        return aFloatP;
    }

    public byte[] getByteP() {
        return aByteP;
    }

    public double[] getDoubleP() {
        return aDoubleP;
    }

    public boolean[] getBooleanP() {
        return aBooleanP;
    }

    public short[] getShortP() {
        return aShortP;
    }

    public char[] getCharacterP() {
        return aCharacterP;
    }

    public char[] getCharacterPClob() {
        return aCharacterPClob;
    }

    public ArraysTest[] getArraysTest() {
        return aArraysTest;
    }

    public void setString(String[] val) {
        aString = val;
    }

    public void setDate(Date[] val) {
        aDate = val;
    }

    public void setInt(Integer[] val) {
        aInt = val;
    }

    public void setLong(Long[] val) {
        aLong = val;
    }

    public void setFloat(Float[] val) {
        aFloat = val;
    }

    public void setByte(Byte[] val) {
        aByte = val;
    }

    public void setDouble(Double[] val) {
        aDouble = val;
    }

    public void setBoolean(Boolean[] val) {
        aBoolean = val;
    }

    public void setShort(Short[] val) {
        aShort = val;
    }

    public void setCharacter(Character[] val) {
        aCharacter = val;
    }

    public void setIntP(int[] val) {
        aIntP = val;
    }

    public void setLongP(long[] val) {
        aLongP = val;
    }

    public void setFloatP(float[] val) {
        aFloatP = val;
    }

    public void setByteP(byte[] val) {
        aByteP = val;
    }

    public void setDoubleP(double[] val) {
        aDoubleP = val;
    }

    public void setBooleanP(boolean[] val) {
        aBooleanP = val;
    }

    public void setShortP(short[] val) {
        aShortP = val;
    }

    public void setCharacterP(char[] val) {
        aCharacterP = val;
    }

    public void setCharacterPClob(char[] val) {
        aCharacterPClob = val;
    }

    public void setArraysTest(ArraysTest[] val) {
        aArraysTest = val;
    }
}
