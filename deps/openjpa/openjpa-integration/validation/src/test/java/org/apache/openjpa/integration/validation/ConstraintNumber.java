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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@Entity(name = "VNUMBER")
@Table(name = "NUMBER_ENTITY")
public class ConstraintNumber implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    @Transient
    private static final long negative = -99;

    @Transient
    private static final long positive = 99;

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Min(value = 0)
    private long minZero;

    @Basic
    private long maxZero;   // @Max(value = 0) constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintNumber createInvalidMin() {
        ConstraintNumber c = new ConstraintNumber();
        c.setMinZero(negative);
        c.setMaxZero(negative);
        return c;
    }

    public static ConstraintNumber createInvalidMax() {
        ConstraintNumber c = new ConstraintNumber();
        c.setMinZero(positive);
        c.setMaxZero(positive);
        return c;
    }

    public static ConstraintNumber createInvalidMinMax() {
        ConstraintNumber c = new ConstraintNumber();
        c.setMinZero(negative);
        c.setMaxZero(positive);
        return c;
    }

    public static ConstraintNumber createValid() {
        ConstraintNumber c = new ConstraintNumber();
        c.setMinZero(positive);
        c.setMaxZero(negative);
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintNumber() {
    }

    public long getId() {
        return id;
    }

    public long getMinZero() {
        return minZero;
    }

    public void setMinZero(long d) {
        minZero = d;
    }

    @Max(value = 0)
    public long getMaxZero() {
        return maxZero;
    }

    public void setMaxZero(long d) {
        maxZero = d;
    }
}
