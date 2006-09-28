/**
 *
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
package org.apache.openejb.persistence;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;

/**
 * Mr. Bott's Every Field Bean
 *
 * Taken from openjpa-persistence-jdbc
 */

@Entity
public class AllFieldTypes {

    private short shortField;
    private int intField;
    private boolean booleanField;
    private long longField;
    private float floatField;
    private char charField;
    private double doubleField;
    private byte byteField;
    private String stringField;
    private Date dateField;
    private Set<String> setOfStrings = new HashSet<String>();
    private String[] arrayOfStrings;

    public void setShortField(short shortField) {
        this.shortField = shortField;
    }

    public short getShortField() {
        return this.shortField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public int getIntField() {
        return this.intField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public boolean getBooleanField() {
        return this.booleanField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public long getLongField() {
        return this.longField;
    }

    public void setFloatField(float floatField) {
        this.floatField = floatField;
    }

    public float getFloatField() {
        return this.floatField;
    }

    public void setCharField(char charField) {
        this.charField = charField;
    }

    public char getCharField() {
        return this.charField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public double getDoubleField() {
        return this.doubleField;
    }

    public void setByteField(byte byteField) {
        this.byteField = byteField;
    }

    public byte getByteField() {
        return this.byteField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public String getStringField() {
        return this.stringField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public Date getDateField() {
        return this.dateField;
    }

    public void setSetOfStrings(Set<String> setOfStrings) {
        this.setOfStrings = setOfStrings;
    }

    public Set<String> getSetOfStrings() {
        return this.setOfStrings;
    }

    public void setArrayOfStrings(String[] arrayOfStrings) {
        this.arrayOfStrings = arrayOfStrings;
    }

    public String[] getArrayOfStrings() {
        return this.arrayOfStrings;
    }
}
