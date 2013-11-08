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
import javax.validation.constraints.Pattern;


@Entity(name = "VPATTERN")
@Table(name = "PATTERN_ENTITY")
public class ConstraintPattern implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Pattern(regexp = "^[A-Z0-9-]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "can only contain alphanumeric characters")
    private String myString;

    @Basic
    private String zipcode;     // @Pattern([0-9]) constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintPattern createInvalidString() {
        ConstraintPattern c = new ConstraintPattern();
        c.setMyString("a1!b2@c3#");
        c.setZipcode("90210");
        return c;
    }

    public static ConstraintPattern createInvalidZipcode() {
        ConstraintPattern c = new ConstraintPattern();
        c.setMyString("");
        c.setZipcode("1a2b3c");
        return c;
    }

    public static ConstraintPattern createValid() {
        ConstraintPattern c = new ConstraintPattern();
        c.setMyString("a1b2c3");
        c.setZipcode("90210");
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintPattern() {
    }

    public long getId() {
        return id;
    }

    public String getMyString() {
        return myString;
    }

    public void setMyString(String s) {
        myString = s;
    }

    @Pattern(regexp = "^[0-9]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain numeric characters")
    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String s) {
        zipcode = s;
    }
}
