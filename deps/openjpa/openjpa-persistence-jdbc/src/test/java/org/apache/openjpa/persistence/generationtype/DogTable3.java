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
package org.apache.openjpa.persistence.generationtype;

import java.io.*;
import javax.persistence.*;

@Entity(name = "DogTable3")
@Table(name = "DOGTABLES", schema = "SCHEMA3")
public class DogTable3 implements Serializable

{
    @Id
    @TableGenerator(name = "Dog_Gen3", table = "ID_Gen3", schema="SCHEMA3G",
            pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", 
            pkColumnValue = "ID2", initialValue = 100, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Dog_Gen3")
    private int id2;

    private String name;

    private float price;

    private boolean domestic;

    public DogTable3() {
        super();

    }

    public DogTable3(String name) {
        this.name = name;

    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id) {
        this.id2 = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {

        this.price = price;
    }

    public boolean isDomestic() {
        return domestic;
    }

    public void setDomestic(boolean domestic) {
        this.domestic = domestic;
    }
}
