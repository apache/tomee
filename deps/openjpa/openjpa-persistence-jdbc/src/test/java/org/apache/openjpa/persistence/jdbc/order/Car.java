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
package org.apache.openjpa.persistence.jdbc.order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="OC_CAR")
public class Car {

    @Id
    @GeneratedValue
    private int id;
        
    @ManyToOne
    private Owner owner;
    
    @Basic
    private int modelYear;
    
    @Basic
    private String make;
    
    @Basic
    private String model;

    public Car() {        
    }
    
    public Car(int year, String make, String model){
        this.modelYear = year;
        this.make = make;
        this.model = model;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setModelYear(int year) {
        this.modelYear = year;
    }

    public int getModelYear() {
        return modelYear;
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

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Car) {
            Car car = (Car)obj;
            return getId() == car.getId() &&
                getMake().equals(car.getMake()) &&
                getModel().equals(car.getModel()) &&
                getModelYear() == car.getModelYear();
        }
        return false;
    }
}
