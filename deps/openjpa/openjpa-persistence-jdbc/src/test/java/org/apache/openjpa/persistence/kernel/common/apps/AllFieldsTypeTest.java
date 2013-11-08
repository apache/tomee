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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Used in testing; should be enhanced.
 */
@Entity
@Table(name = "afieldstt")
public class AllFieldsTypeTest {

    @Id
    private int id;
    private int testint;
    private long testlong;
    private short testshort;
    private float testfloat;
    private double testdouble;
    private byte testbyte;
    private boolean testboolean;
    private char testchar;
    private String testString;
    private String testBigString;
    private Date testDate;
    private Calendar testCalendar;
    private Object testObject;
    private BigInteger testBigInteger;
    @Column(scale=8, precision=12)
    private BigDecimal testBigDecimal;

    public Date getTestDate() {
        return this.testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public void setTestCalendar(Calendar testCalendar) {
        this.testCalendar = testCalendar;
    }

    public Calendar getTestCalendar() {
        return this.testCalendar;
    }

    public Object getTestObject() {
        return this.testObject;
    }

    public void setTestObject(Object testObject) {
        this.testObject = testObject;
    }

    public char getTestchar() {
        return this.testchar;
    }

    public void setTestchar(char testchar) {
        this.testchar = testchar;
    }

    public int getTestint() {
        return this.testint;
    }

    public void setTestint(int testint) {
        this.testint = testint;
    }

    public short getTestshort() {
        return this.testshort;
    }

    public void setTestshort(short testshort) {
        this.testshort = testshort;
    }

    public long getTestlong() {
        return this.testlong;
    }

    public void setTestlong(long testlong) {
        this.testlong = testlong;
    }

    public boolean getTestboolean() {
        return this.testboolean;
    }

    public void setTestboolean(boolean testboolean) {
        this.testboolean = testboolean;
    }

    public float getTestfloat() {
        return this.testfloat;
    }

    public void setTestfloat(float testfloat) {
        this.testfloat = testfloat;
    }

    public double getTestdouble() {
        return this.testdouble;
    }

    public void setTestdouble(double testdouble) {
        this.testdouble = testdouble;
    }

    public String getTestString() {
        return this.testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public void setTestBigString(String testBigString) {
        this.testBigString = testBigString;
    }

    public String getTestBigString() {
        return this.testBigString;
    }

    public byte getTestbyte() {
        return this.testbyte;
    }

    public void setTestbyte(byte testbyte) {
        this.testbyte = testbyte;
    }

    public BigInteger getTestBigInteger() {
        return this.testBigInteger;
    }

    public void setTestBigInteger(BigInteger testBigInteger) {
        this.testBigInteger = testBigInteger;
    }

    public BigDecimal getTestBigDecimal() {
        return this.testBigDecimal;
    }

    public void setTestBigDecimal(BigDecimal testBigDecimal) {
        this.testBigDecimal = testBigDecimal;
    }

    public String toString() {
        return "\n{"
            + "testint=" + testint + ";"
            + "testlong=" + testlong + ";"
            + "testshort=" + testshort + ";"
            + "testfloat=" + testfloat + ";"
            + "testdouble=" + testdouble + ";"
            + "testbyte=" + testbyte + ";"
            + "testboolean=" + testboolean + ";"
            + "testchar=" + testchar + ";"
            + "testString=" + testString + ";"
            + "testBigString=" + testBigString + ";"
            + "testDate=" + testDate + ";"
            + "testCalendar=" + testCalendar + ";"
            + "testObject=" + testObject
            + "(" + ((testObject instanceof byte[]) ?
            "(byte[])" + ((byte[]) testObject).length + "" :
            (testObject == null ? "null" :
                testObject.getClass().getName())) + ")"
            + ";"
            + "testBigInteger=" + testBigInteger + ";"
            + "testBigDecimal=" + testBigDecimal + ";"
            + "}";
    }

    public int hashCode() {
        return (int) ((
            testint
                + testlong
                + testshort
                + testfloat
                + testdouble
                + testbyte
                + (testboolean ? 1 : 0)
                + testchar
                + (testString == null ? 0 : testString.hashCode())
                + (testBigString == null ? 0 : testBigString.hashCode())
                + (testDate == null ? 0 : testDate.hashCode())
                + (testCalendar == null ? 0 : testCalendar.hashCode())
                + (testObject == null ? 0 : testObject.hashCode())
                + (testBigInteger == null ? 0 : testBigInteger.hashCode())
                + (testBigDecimal == null ? 0 : testBigDecimal.hashCode())))
            % Integer.MAX_VALUE;
    }

    public boolean equals(Object other) {
        if (other == null && !(other instanceof AllFieldsTypeTest))
            return false;

        AllFieldsTypeTest o = (AllFieldsTypeTest) other;

        return
            o.testint == testint &&
                o.testlong == testlong &&
                o.testshort == testshort &&
                o.testfloat == testfloat &&
                o.testdouble == testdouble &&
                o.testbyte == testbyte &&
                o.testboolean == testboolean &&
                o.testchar == testchar &&
                eq(o.testString, testString) &&
                eq(o.testBigString, testBigString) &&
                eq(o.testDate, testDate) &&
                eq(o.testCalendar, testCalendar) &&
                eq(o.testObject, testObject) &&
                eq(o.testBigInteger, o.testBigInteger) &&
                eq(o.testBigDecimal, o.testBigDecimal);
    }

    private boolean eq(Object a, Object b) {
        if (a == b)
            return true;
        if (a == null && b != null)
            return false;
        if (a != null && b == null)
            return false;

        // OK, this is stupid, but we want to special-case blobs
        // thay are byte arrays, since new byte [] { 1, 2} does not
        // equals inew byte [] { 1, 2}
        if (a instanceof byte[] && b instanceof byte[])
            return Arrays.equals((byte[]) a, (byte[]) b);

        return a.equals(b);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

