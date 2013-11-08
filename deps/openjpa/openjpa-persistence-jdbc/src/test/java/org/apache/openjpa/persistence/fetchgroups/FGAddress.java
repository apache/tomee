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
package org.apache.openjpa.persistence.fetchgroups;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class FGAddress {
    @Id
    private int id;

    @Basic
    private String street;

    @Basic
    private String city;

    @Basic
    private String state;

    @Basic
    private int zip;

    public FGAddress() {

    }

    public FGAddress(int id, String street, String city, String state,
            int zip) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FGAddress(id=").append(this.id).append(")");
        sb.append(": street=").append(getStreet());
        sb.append(": city=").append(getCity());
        sb.append(": state=").append(getState());
        sb.append(": zip=").append(getZip());

        return new String(sb);
    }

}
