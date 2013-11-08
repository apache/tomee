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
package org.apache.openjpa.persistence.fetchgroups;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

@Entity
@DiscriminatorValue("1")
@FetchGroups( {
        @FetchGroup(name = "MDataFetchGroup",
                attributes = { @FetchAttribute(name = "mData") }),
        @FetchGroup(name = "EmployeesFetchGroup",
                attributes = { @FetchAttribute(name = "employees") }) })
public class FGManager extends FGEmployee {
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    Collection<FGEmployee> employees;

    @Basic(fetch = FetchType.LAZY)
    private String mData;

    public FGManager() {
        super();
        employees = new ArrayList<FGEmployee>();
    }

    public FGManager(int id, String firstName, String lastName, String desc,
            FGDepartment dept, FGAddress address, FGManager manager,
            String rating, Collection<FGEmployee> employees, String mData) {
        super(id, firstName, lastName, desc, dept, address, manager, rating);
        this.employees = new ArrayList<FGEmployee>();
        this.employees.addAll(employees);
        this.mData = mData;
    }

    public String getMData() {
        return mData;
    }

    public void setMData(String data) {
        mData = data;
    }

    public Collection<FGEmployee> getEmployees() {
        return employees;
    }

}
