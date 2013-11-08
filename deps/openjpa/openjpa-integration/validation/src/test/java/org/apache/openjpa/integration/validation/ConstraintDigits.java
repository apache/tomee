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
import javax.validation.constraints.Digits;


@Entity(name = "VDIGITS")
@Table(name = "DIGITS_ENTITY")
public class ConstraintDigits implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    @Transient
    private static final BigDecimal SIX_DIGITS = new BigDecimal("666666.666666");

    @Transient
    private static final BigDecimal FIVE_DIGITS = new BigDecimal("55555.55555");

    @Transient
    private static final BigDecimal ONE_DIGITS = new BigDecimal("1.1");

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Digits(integer = 2, fraction = 2)
    private BigDecimal twoDigits;

    @Basic
    private BigDecimal fiveDigits;  // @Digits(5,5) constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintDigits createInvalidTwoDigits() {
        ConstraintDigits c = new ConstraintDigits();
        c.setTwoDigits(FIVE_DIGITS);
        c.setFiveDigits(FIVE_DIGITS);
        return c;
    }

    public static ConstraintDigits createInvalidFiveDigits() {
        ConstraintDigits c = new ConstraintDigits();
        c.setTwoDigits(ONE_DIGITS);
        c.setFiveDigits(SIX_DIGITS);
        return c;
    }

    public static ConstraintDigits createInvalidDigits() {
        ConstraintDigits c = new ConstraintDigits();
        c.setTwoDigits(FIVE_DIGITS);
        c.setFiveDigits(SIX_DIGITS);
        return c;
    }

    public static ConstraintDigits createValid() {
        ConstraintDigits c = new ConstraintDigits();
        // extra leading zeros only count as 1 digit
        c.setTwoDigits("00000000.1");
        // as long as one of integer/fraction is supplied and valid
        c.setFiveDigits("1234");
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintDigits() {
    }

    public long getId() {
        return id;
    }

    public BigDecimal getTwoDigits() {
        return twoDigits;
    }

    public void setTwoDigits(BigDecimal d) {
        twoDigits = d;
    }

    public void setTwoDigits(String s) {
        twoDigits = toBigDecimal(s);
    }

    @Digits(integer = 5, fraction = 5)
    public BigDecimal getFiveDigits() {
        return fiveDigits;
    }

    public void setFiveDigits(BigDecimal d) {
        fiveDigits = d;
    }
    
    public void setFiveDigits(String s) {
        fiveDigits = toBigDecimal(s);
    }
    

    private BigDecimal toBigDecimal(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }
}
