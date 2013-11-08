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

import javax.persistence.*;

import org.apache.openjpa.persistence.DataCache;

@Entity
@DataCache

public class Supplier {

    @Id  int sid;
    @Column(length=20)
    String name;

    @ManyToMany
    List<PartBase> supplies = new ArrayList<PartBase>();

    @Version 
    long version;

    public Supplier(){}

    public Supplier(int sid, String name){
        this.sid=sid;
        this.name=name;
    }

    public Supplier addPart( PartBase p ) {
        supplies.add(p);
        p.getSuppliers().add(this);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public Collection<PartBase> getSupplies() {
        return supplies;
    }

    public void setSupplies(List<PartBase> supplies) {
        this.supplies = supplies;
    }

    public String toString() {

        return "Supplier:"+sid+" name:+"+name;
    }
}
