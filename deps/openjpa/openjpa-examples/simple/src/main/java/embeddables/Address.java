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
package embeddables;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class Address {
    @Basic
    private String street;
    @Basic
    private String city;
    @Basic
    private String state;
    @Basic
    private Integer zip;

    // Relationship from an Embeddable to an Entity
    @ManyToOne(cascade = CascadeType.ALL)
    Coordinates coordinates;

    public Address() {

    }

    public Address(String street, String city, String state, Integer zip, Coordinates c) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        coordinates = c;
    }

    public String toString() {
        return street + " " + city + ", " + state + " " + zip;
    }
}
