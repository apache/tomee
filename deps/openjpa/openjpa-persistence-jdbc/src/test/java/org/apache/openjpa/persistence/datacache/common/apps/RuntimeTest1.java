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
package org.apache.openjpa.persistence.datacache.common.apps;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Used in testing; should be enhanced.
 */
@Entity
@Table(name="DATART1")
@DiscriminatorValue("dataRt1")
public class RuntimeTest1
    implements Serializable {

    public static final String someStaticField = "someField";

    private byte byteField;
    private boolean booleanField;
    private char charField;
    private double doubleField;
    private float floatField;
    private int intField;
    private long longField;
    private short shortField;
    private String stringField;
    private BigInteger bigIntegerField;
    private BigDecimal bigDecimalField;
    @Temporal(TemporalType.DATE)
    private Date dateField;
    private Locale localeField;
    private Byte byteObjfield;
    private Boolean booleanObjField;
    private Character charObjField;
    private Double doubleObjField;
    private Float floatObjField;
    private Integer intObjField;
    private Long longObjField;
    private Short shortObjField;

    // transactional only
    @OneToOne(fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private TransactionalClassPC transField;
    public String transString;

    // relations
    @OneToOne(fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private RuntimeTest1 selfOneOne;

    @OneToMany(mappedBy = "selfOneOne",
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private Set<RuntimeTest1> selfOneMany = new HashSet<RuntimeTest1>();

    public RuntimeTest1() {
    }

    public RuntimeTest1(String str, int i) {
        stringField = str;
        intField = i;
    }

    public byte getByteField() {
        return this.byteField;
    }

    public void setByteField(byte byteField) {
        this.byteField = byteField;
    }

    public boolean getBooleanField() {
        return this.booleanField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public char getCharField() {
        return this.charField;
    }

    public void setCharField(char charField) {
        this.charField = charField;
    }

    public double getDoubleField() {
        return this.doubleField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public float getFloatField() {
        return this.floatField;
    }

    public void setFloatField(float floatField) {
        this.floatField = floatField;
    }

    public int getIntField() {
        return this.intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public long getLongField() {
        return this.longField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public short getShortField() {
        return this.shortField;
    }

    public void setShortField(short shortField) {
        this.shortField = shortField;
    }

    public String getStringField() {
        return this.stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public BigInteger getBigIntegerField() {
        return this.bigIntegerField;
    }

    public void setBigIntegerField(BigInteger bigIntegerField) {
        this.bigIntegerField = bigIntegerField;
    }

    public BigDecimal getBigDecimalField() {
        return this.bigDecimalField;
    }

    public void setBigDecimalField(BigDecimal bigDecimalField) {
        this.bigDecimalField = bigDecimalField;
    }

    public Date getDateField() {
        return this.dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public Locale getLocaleField() {
        return this.localeField;
    }

    public void setLocaleField(Locale localeField) {
        this.localeField = localeField;
    }

    public Byte getByteObjfield() {
        return this.byteObjfield;
    }

    public void setByteObjfield(Byte byteObjfield) {
        this.byteObjfield = byteObjfield;
    }

    public Boolean getBooleanObjField() {
        return this.booleanObjField;
    }

    public void setBooleanObjField(Boolean booleanObjField) {
        this.booleanObjField = booleanObjField;
    }

    public Character getCharObjField() {
        return this.charObjField;
    }

    public void setCharObjField(Character charObjField) {
        this.charObjField = charObjField;
    }

    public Double getDoubleObjField() {
        return this.doubleObjField;
    }

    public void setDoubleObjField(Double doubleObjField) {
        this.doubleObjField = doubleObjField;
    }

    public Float getFloatObjField() {
        return this.floatObjField;
    }

    public void setFloatObjField(Float floatObjField) {
        this.floatObjField = floatObjField;
    }

    public Integer getIntObjField() {
        return this.intObjField;
    }

    public void setIntObjField(Integer intObjField) {
        this.intObjField = intObjField;
    }

    public Long getLongObjField() {
        return this.longObjField;
    }

    public void setLongObjField(Long longObjField) {
        this.longObjField = longObjField;
    }

    public Short getShortObjField() {
        return this.shortObjField;
    }

    public void setShortObjField(Short shortObjField) {
        this.shortObjField = shortObjField;
    }

    public TransactionalClassPC getTransField() {
        return this.transField;
    }

    public void setTransField(TransactionalClassPC transField) {
        this.transField = transField;
    }

    public RuntimeTest1 getSelfOneOne() {
        return this.selfOneOne;
    }

    public void setSelfOneOne(RuntimeTest1 selfOneOne) {
        this.selfOneOne = selfOneOne;
    }

    public Set getSelfOneMany() {
        return this.selfOneMany;
    }

    public void setSelfOneMany(Set selfOneMany) {
        this.selfOneMany = selfOneMany;
    }
}
