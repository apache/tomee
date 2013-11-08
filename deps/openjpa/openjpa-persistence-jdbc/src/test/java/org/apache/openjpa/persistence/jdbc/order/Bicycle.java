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

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Bicycle {
    
    @Column(name="bike_brand")
    private String brand;
    
    @Column(name="bike_model")
    private String model;

    public Bicycle() {
    }

    public Bicycle(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Bicycle) {
            Bicycle bike = (Bicycle)obj;
            return getBrand().equals(bike.getBrand()) &&
              getModel().equals(bike.getModel());
        }
        return false;
    }
}
