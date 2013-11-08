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
package org.apache.openjpa.slice;

import javax.persistence.*;

@Entity
public class Car {
    private String vin;
    
    private String model;

    private Manufacturer maker;

    @Id
    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    @Basic
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    public Manufacturer getMaker() {
        return maker;
    }

    public void setMaker(Manufacturer maker) {
        this.maker = maker;
    }
}
