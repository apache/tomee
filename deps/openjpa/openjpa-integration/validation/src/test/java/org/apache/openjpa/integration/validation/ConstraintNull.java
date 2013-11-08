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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;


@Entity(name = "VNULL")
@Table(name = "NULL_ENTITY")
public class ConstraintNull implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Null
    private String nullRequired;

    @Basic
    private String nullInvalid;     // @NotNull constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintNull createInvalidNotNull() {
        ConstraintNull c = new ConstraintNull();
        return c;
    }

    public static ConstraintNull createInvalidNull() {
        ConstraintNull c = new ConstraintNull();
        c.setNullInvalid("not null");
        c.setNullRequired("not null");
        return c;
    }

    public static ConstraintNull createValid() {
        ConstraintNull c = new ConstraintNull();
        c.setNullInvalid("not null");
        c.setNullRequired(null);
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintNull() {
    }

    public long getId() {
        return id;
    }

    public String getNullRequired() {
        return nullRequired;
    }

    public void setNullRequired(String s) {
        nullRequired = s;
    }

    @NotNull
    public String getNullInvalid() {
        return nullInvalid;
    }

    public void setNullInvalid(String s) {
        nullInvalid = s;
    }

}
