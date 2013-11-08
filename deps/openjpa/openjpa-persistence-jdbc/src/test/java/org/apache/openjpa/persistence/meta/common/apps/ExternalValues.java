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
package org.apache.openjpa.persistence.meta.common.apps;


import java.util.UUID;

import javax.persistence.Entity;

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

@Entity
public class ExternalValues {

    private boolean booleanToShort;
    private byte byteToDouble;
    private int intToFloat;
    private long longToChar;
    private short shortToString;
    private float floatToBoolean;
    private double doubleToByte;
    private char charToInt;
    private String stringToLong;
    @Persistent
    @Externalizer("toString")        
    @Factory("UUID.fromString")
    private UUID    uuid;

    public boolean getBooleanToShort() {
        return booleanToShort;
    }

    public void setBooleanToShort(boolean b) {
        booleanToShort = b;
    }

    public byte getByteToDouble() {
        return byteToDouble;
    }

    public void setByteToDouble(byte b) {
        byteToDouble = b;
    }

    public int getIntToFloat() {
        return intToFloat;
    }

    public void setIntToFloat(int i) {
        intToFloat = i;
    }

    public long getLongToChar() {
        return longToChar;
    }

    public void setLongToChar(long l) {
        longToChar = l;
    }

    public short getShortToString() {
        return shortToString;
    }

    public void setShortToString(short s) {
        shortToString = s;
    }

    public double getDoubleToByte() {
        return doubleToByte;
    }

    public void setDoubleToByte(double d) {
        doubleToByte = d;
    }

    public float getFloatToBoolean() {
        return floatToBoolean;
    }

    public void setFloatToBoolean(float f) {
        floatToBoolean = f;
    }

    public char getCharToInt() {
        return charToInt;
    }

    public void setCharToInt(char c) {
        charToInt = c;
    }

    public String getStringToLong() {
        return stringToLong;
    }

    public void setStringToLong(String s) {
        stringToLong = s;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
