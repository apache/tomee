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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */
@Entity
@FetchGroup(name = "address.country",
    attributes = @FetchAttribute(name = "country"))
public class PCAddress {

    private String street;
    private String city;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCCountry country;

    protected PCAddress() {
        super();
    }

    public PCAddress(String street, String city, PCCountry country) {
        setStreet(street);
        setCity(city);
        setCountry(country);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public PCCountry getCountry() {
        return country;
    }

    public void setCountry(PCCountry country) {
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public static Object reflect(PCAddress instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCAddress.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
