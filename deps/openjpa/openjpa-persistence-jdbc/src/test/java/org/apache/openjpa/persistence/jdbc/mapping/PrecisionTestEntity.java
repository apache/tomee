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
package org.apache.openjpa.persistence.jdbc.mapping;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class PrecisionTestEntity {
    @Id
    @GeneratedValue
    private int id;
    @Version
    private int version;

    private double primDbl;
    private Double dbl;
    private BigDecimal bigDecimal;

    @Column(precision = 10)
    private double primDblPrecis;
    @Column(precision = 10)
    private Double dblPrecis;
    @Column(precision = 10)
    private BigDecimal bigDecimalPrecis;

    @Column(scale = 10)
    private double primDblScale;
    @Column(scale = 10)
    private Double dblScale;
    @Column(scale = 10)
    private BigDecimal bigDecimalScale;

    @Column(precision = 10, scale = 10)
    private double primDblPrecisScale;
    @Column(precision = 10, scale = 10)
    private Double dblPrecisScale;
    @Column(precision = 10, scale = 10)
    private BigDecimal bigDecimalPrecisScale;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public double getPrimDbl() {
        return primDbl;
    }

    public void setPrimDbl(double primDbl) {
        this.primDbl = primDbl;
    }

    public Double getDbl() {
        return dbl;
    }

    public void setDbl(Double dbl) {
        this.dbl = dbl;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public double getPrimDblPrecis() {
        return primDblPrecis;
    }

    public void setPrimDblPrecis(double primDblPrecis) {
        this.primDblPrecis = primDblPrecis;
    }

    public Double getDblPrecis() {
        return dblPrecis;
    }

    public void setDblPrecis(Double dblPrecis) {
        this.dblPrecis = dblPrecis;
    }

    public BigDecimal getBigDecimalPrecis() {
        return bigDecimalPrecis;
    }

    public void setBigDecimalPrecis(BigDecimal bigDecimalPrecis) {
        this.bigDecimalPrecis = bigDecimalPrecis;
    }

    public double getPrimDblScale() {
        return primDblScale;
    }

    public void setPrimDblScale(double primDblScale) {
        this.primDblScale = primDblScale;
    }

    public Double getDblScale() {
        return dblScale;
    }

    public void setDblScale(Double dblScale) {
        this.dblScale = dblScale;
    }

    public BigDecimal getBigDecimalScale() {
        return bigDecimalScale;
    }

    public void setBigDecimalScale(BigDecimal bigDecimalScale) {
        this.bigDecimalScale = bigDecimalScale;
    }

    public double getPrimDblPrecisScale() {
        return primDblPrecisScale;
    }

    public void setPrimDblPrecisScale(double primDblPrecisScale) {
        this.primDblPrecisScale = primDblPrecisScale;
    }

    public Double getDblPrecisScale() {
        return dblPrecisScale;
    }

    public void setDblPrecisScale(Double dblPrecisScale) {
        this.dblPrecisScale = dblPrecisScale;
    }

    public BigDecimal getBigDecimalPrecisScale() {
        return bigDecimalPrecisScale;
    }

    public void setBigDecimalPrecisScale(BigDecimal bigDecimalPrecisScale) {
        this.bigDecimalPrecisScale = bigDecimalPrecisScale;
    }
}
