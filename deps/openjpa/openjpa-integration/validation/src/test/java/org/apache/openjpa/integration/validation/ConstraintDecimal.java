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
package org.apache.openjpa.integration.validation;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;


@Entity(name = "VDECIMAL")
@Table(name = "DECIMAL_ENTITY")
public class ConstraintDecimal implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    @Transient
    private static final BigDecimal negative = new BigDecimal(-99.99);

    @Transient
    private static final BigDecimal positive = new BigDecimal(99.99);

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @DecimalMin(value = "0")
    private BigDecimal minZero;

    @Basic
    private BigDecimal maxZero;     // @DecimalMax(value = "0") constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintDecimal createInvalidMin() {
        ConstraintDecimal c = new ConstraintDecimal();
        c.setMinZero(negative);
        c.setMaxZero(negative);
        return c;
    }

    public static ConstraintDecimal createInvalidMax() {
        ConstraintDecimal c = new ConstraintDecimal();
        c.setMinZero(positive);
        c.setMaxZero(positive);
        return c;
    }

    public static ConstraintDecimal createInvalidMinMax() {
        ConstraintDecimal c = new ConstraintDecimal();
        c.setMinZero(negative);
        c.setMaxZero(positive);
        return c;
    }

    public static ConstraintDecimal createValid() {
        ConstraintDecimal c = new ConstraintDecimal();
        c.setMinZero(positive);
        c.setMaxZero(negative);
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintDecimal() {
    }

    public long getId() {
        return id;
    }

    public BigDecimal getMinZero() {
        return minZero;
    }

    public void setMinZero(BigDecimal d) {
        minZero = d;
    }

    @DecimalMax(value = "0")
    public BigDecimal getMaxZero() {
        return maxZero;
    }

    public void setMaxZero(BigDecimal d) {
        maxZero = d;
    }
}
