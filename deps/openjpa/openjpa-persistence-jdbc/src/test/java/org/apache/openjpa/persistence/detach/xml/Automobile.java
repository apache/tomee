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
package org.apache.openjpa.persistence.detach.xml;

import java.util.Collection;

public class Automobile {
    
    private long id;
    
    private String make;
    private String model;
        
    // Many To Many
    private Collection<Passenger> passengers;
    
    // One to One
    private Driver primaryDriver;

    // Many to One
    private Owner owner;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getMake() {
        return make;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setPassengers(Collection<Passenger> passengers) {
        this.passengers = passengers;
    }

    public Collection<Passenger> getPassengers() {
        return passengers;
    }

    public void setPrimaryDriver(Driver primaryDriver) {
        this.primaryDriver = primaryDriver;
    }

    public Driver getPrimaryDriver() {
        return primaryDriver;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }
}
