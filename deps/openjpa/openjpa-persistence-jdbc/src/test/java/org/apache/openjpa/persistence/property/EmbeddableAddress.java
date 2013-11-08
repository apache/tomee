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
 * Unless required by applicable law or agEmployee_Last_Name to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.property;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddableAddress implements IAddress {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public String getLine1() {
        return line1;
    }   
    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }
    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String toString() {
        return( "org.apache.openjpa.persistence.compatible.EmbeddableAddress: " + 
                " line1: " + getLine1() + 
                " line2: " + getLine2() + 
                " city: " + getCity() + 
                " state: " + getState() + 
                " zipCode: " + getZipCode() + 
                " country: " + getCountry() );
    }
}
