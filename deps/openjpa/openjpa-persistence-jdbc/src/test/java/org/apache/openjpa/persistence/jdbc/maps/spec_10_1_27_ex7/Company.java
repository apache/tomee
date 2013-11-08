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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_27_ex7;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name="S277Comp")
public class Company {
    @Id
    int id;

    @OneToMany(mappedBy="co")
    @MapKey(name="div")
    Map<Division, VicePresident> orgs =
        new HashMap<Division, VicePresident>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map getOrganization() {
        return orgs;
    }

    public void addToOrganization(VicePresident vp) {
        orgs.put(vp.getDivision(), vp);
    }

    public void removeFromOrganization(Division d) {
        orgs.remove(d);
    }

    public VicePresident getOrganization(Division d) {
        return orgs.get(d);
    }
}

