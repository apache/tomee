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

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import static javax.persistence.InheritanceType.SINGLE_TABLE;

@Entity
@Table(name="OC_OWNER")
@Inheritance(strategy=SINGLE_TABLE)
public class Owner extends Person {
    
    // bidirectional one-to-many w/ default join column
    @OneToMany(mappedBy="owner", cascade=CascadeType.ALL)
    @JoinTable(name="car_o2m_table")
    @OrderColumn(name="car_o2m_order")
    private Collection<Car> cars;

    // unidirectional one-to-many w/ join column
    @OneToMany(cascade=CascadeType.ALL)
    @JoinTable(name="home_o2m_table")
    @OrderColumn
    private Collection<Home> homes;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="widget_m2m_table")
    @OrderColumn
    private Collection<Widget> widgets;
    
    // element collection
    @ElementCollection
    @CollectionTable(name="bike_table")
    @OrderColumn(name="bike_coll_order")
    private Collection<Bicycle> bikeColl;

        
    public void setCars(Collection<Car> cars) {
        this.cars = cars;
    }

    public Collection<Car> getCars() {
        return cars;
    }

    public void setHomes(Collection<Home> homes) {
        this.homes = homes;
    }

    public Collection<Home> getHomes() {
        return homes;
    }

    public void setBikeColl(Collection<Bicycle> bikeColl) {
        this.bikeColl = bikeColl;
    }

    public Collection<Bicycle> getBikeColl() {
        return bikeColl;
    }

    public void setWidgets(Collection<Widget> widgets) {
        this.widgets = widgets;
    }

    public Collection<Widget> getWidgets() {
        return widgets;
    }    
}
