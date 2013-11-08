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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity(name="VPerson")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Person implements IPerson, Serializable {
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Pattern(regexp = "^[A-Z0-9-]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain alphanumeric characters")
    private String firstName;   // @NotNull is on IPerson getter

    @Basic
    @Pattern(regexp = "^[A-Z0-9-]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain alphanumeric characters")
    private String lastName;    // @NotNull is on IPerson getter

    @OneToOne
    @NotNull
    @Valid
    private Address homeAddress;

    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return this.firstName;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return this.lastName;
    }


    public void setHomeAddress(IAddress homeAddress) {
        this.homeAddress = (Address) homeAddress;
    }

    public IAddress getHomeAddress() {
        return this.homeAddress;
    }


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

}
