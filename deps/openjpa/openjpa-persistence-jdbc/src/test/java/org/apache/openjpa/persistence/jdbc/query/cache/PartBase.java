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
package org.apache.openjpa.persistence.jdbc.query.cache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.openjpa.persistence.DataCache;


@Entity
@DataCache
public class PartBase extends Part  {

    double cost;
    double mass;
    int backOrder;

    @ManyToMany(mappedBy="supplies")
    protected List<Supplier> suppliers = new ArrayList<Supplier>();

    public PartBase() {}

    public PartBase(int partno, String name, double cost, double mass){
        this.partno=partno;
        this.name = name;
        this.cost = cost;
        this.mass= mass;
        this.backOrder=0;
        this.inventory=0;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public Collection<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public String toString() {
        String sup= "";
        if (getSuppliers()!=null)
            for (Supplier s : getSuppliers()){
                sup= sup+s.sid+",";
            }
        return "PartBase:"+partno+" name:+"+name+" cost:"+cost+" mass:"+
            mass+" supplies=["+sup+"]";
    }

    public int getBackOrder() {
        return backOrder;
    }

    public void setBackOrder(int backOrder) {
        this.backOrder = backOrder;
    }
}
