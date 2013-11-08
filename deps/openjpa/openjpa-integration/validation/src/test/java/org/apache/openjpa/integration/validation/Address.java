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
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity(name="VAddress")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Address implements IAddress, Serializable {
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Pattern(regexp = "^.*$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can contain any character")
    private String streetAddress;   // @NotNull is on IAddress getter

    @Basic
    @Pattern(regexp = "^[A-Z .-]*$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain alpha, '.', '-' and ' ' characters")
    private String city;            // @NotNull is on IAddress getter

    @Basic
    @Size(min = 2, max = 2)
    @Pattern(regexp = "^[A-Z]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain alpha characters")
    private String state;           // @NotNull is on IAddress getter

    @Basic
    @Size(min = 5, max = 5)
    @Pattern(regexp = "^[0-9]+$", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "can only contain numeric characters")
    private String postalCode;      // @NotNull is on IAddress getter

    @Basic
    private String phoneNumber;

    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return this.streetAddress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}
