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
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.PersistentMap;

/**
 * Used in testing; should be enhanced.
 */
@Entity
public class SCOTest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @PersistentMap
    @KeyColumn(name = "strintkey")
    private Map<String, Integer> strIntMap;

    @PersistentMap
    @KeyColumn(name = "intlngkey")
    private Map<Integer, Long> intLongMap;

    @PersistentMap
    @KeyColumn(name = "lngfltkey")
    private Map<Long, Float> longFloatMap;

    @PersistentMap
    @KeyColumn(name = "fltbtekey")
    private Map<Float, Byte> floatByteMap;

    @PersistentMap
    @KeyColumn(name = "btedbkey")
    private Map<Byte, Double> byteDoubleMap;

    @PersistentMap
    @KeyColumn(name = "dbchkey")
    private Map<Double, Character> doubleCharMap;

    @PersistentMap
    @KeyColumn(name = "chblkey")
    private Map<Character, Boolean> charBooleanMap;

    @PersistentMap
    @KeyColumn(name = "dtstrkey")
    private Map<Date, String> dateStrMap;

    @PersistentMap
    @KeyColumn(name = "bgdckey")
    private Map<BigDecimal, BigInteger> bigDecimalBigIntegerMap;

    @PersistentCollection
    private Collection<String> cString;

    @PersistentCollection
    private Collection<Integer> cInteger;

    @PersistentCollection
    private Collection<Long> cLong;

    @PersistentCollection
    private Collection<Float> cFloat;

    @PersistentCollection
    private Collection<Byte> cByte;

    @PersistentCollection
    private Collection<Double> cDouble;

    @PersistentCollection
    private Collection<Boolean> cBoolean;

    @PersistentCollection
    private Collection<Short> cShort;

    @PersistentCollection
    private Collection<Date> cDate;

    @PersistentCollection
    private Collection<Character> cCharacter;

    @PersistentCollection
    private Collection<BigInteger> cBigInteger;

    @PersistentCollection
    private Collection<BigDecimal> cBigDecimal;

    public SCOTest() {
    }

    public int getId() {
        return this.id;
    }

    public void setBigDecimalBigIntegerMap(Map bigDecimalBigIntegerMap) {
        this.bigDecimalBigIntegerMap = bigDecimalBigIntegerMap;
    }

    public Map getBigDecimalBigIntegerMap() {
        return bigDecimalBigIntegerMap;
    }

    public void setStrIntMap(Map strIntMap) {
        this.strIntMap = strIntMap;
    }

    public Map getStrIntMap() {
        return strIntMap;
    }

    public void setIntLongMap(Map intLongMap) {
        this.intLongMap = intLongMap;
    }

    public Map getIntLongMap() {
        return intLongMap;
    }

    public void setLongFloatMap(Map longFloatMap) {
        this.longFloatMap = longFloatMap;
    }

    public Map getLongFloatMap() {
        return longFloatMap;
    }

    public void setFloatByteMap(Map floatByteMap) {
        this.floatByteMap = floatByteMap;
    }

    public Map getFloatByteMap() {
        return floatByteMap;
    }

    public void setByteDoubleMap(Map byteDoubleMap) {
        this.byteDoubleMap = byteDoubleMap;
    }

    public Map getByteDoubleMap() {
        return byteDoubleMap;
    }

    public void setDoubleCharMap(Map doubleCharMap) {
        this.doubleCharMap = doubleCharMap;
    }

    public Map getDoubleCharMap() {
        return doubleCharMap;
    }

    public void setCharBooleanMap(Map charBooleanMap) {
        this.charBooleanMap = charBooleanMap;
    }

    public Map getCharBooleanMap() {
        return charBooleanMap;
    }

    public void setDateStrMap(Map dateStrMap) {
        this.dateStrMap = dateStrMap;
    }

    public Map getDateStrMap() {
        return dateStrMap;
    }

    public void setCString(Collection cString) {
        this.cString = cString;
    }

    public Collection getCString() {
        return cString;
    }

    public void setCInteger(Collection cInteger) {
        this.cInteger = cInteger;
    }

    public Collection getCInteger() {
        return cInteger;
    }

    public void setCLong(Collection cLong) {
        this.cLong = cLong;
    }

    public Collection getCLong() {
        return cLong;
    }

    public void setCCharacter(Collection cCharacter) {
        this.cCharacter = cCharacter;
    }

    public Collection getCCharacter() {
        return cCharacter;
    }

    public void setCFloat(Collection cFloat) {
        this.cFloat = cFloat;
    }

    public Collection getCFloat() {
        return cFloat;
    }

    public void setCByte(Collection cByte) {
        this.cByte = cByte;
    }

    public Collection getCByte() {
        return cByte;
    }

    public void setCDouble(Collection cDouble) {
        this.cDouble = cDouble;
    }

    public Collection getCDouble() {
        return cDouble;
    }

    public void setCBoolean(Collection cBoolean) {
        this.cBoolean = cBoolean;
    }

    public Collection getCBoolean() {
        return cBoolean;
    }

    public void setCShort(Collection cShort) {
        this.cShort = cShort;
    }

    public Collection getCShort() {
        return cShort;
    }

    public void setCDate(Collection cDate) {
        this.cDate = cDate;
    }

    public Collection getCDate() {
        return cDate;
    }

    public void setCBigInteger(Collection cBigInteger) {
        this.cBigInteger = cBigInteger;
    }

    public Collection getCBigInteger() {
        return cBigInteger;
    }

    public void setCBigDecimal(Collection cBigDecimal) {
        this.cBigDecimal = cBigDecimal;
    }

    public Collection getCBigDecimal() {
        return cBigDecimal;
    }
}
