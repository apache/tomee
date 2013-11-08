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

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.LoadFetchGroup;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Default inheritance strategy
@DiscriminatorColumn(name = "EMP_TYPE",
        discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@FetchGroups( {
        @FetchGroup(name = "AddressFetchGroup",
                attributes = { @FetchAttribute(name = "address") }),
        @FetchGroup(name = "RatingFetchGroup",
                attributes = { @FetchAttribute(name = "rating") }),
        @FetchGroup(name = "ManagerFetchGroup1A",
                attributes = { @FetchAttribute(name = "manager",
                recursionDepth = 1) }),
        @FetchGroup(name = "ManagerFetchGroup1B",
                attributes = { @FetchAttribute(name = "manager",
                recursionDepth = -1) }),
        @FetchGroup(name = "ManagerFetchGroup2",
                attributes = { @FetchAttribute(name = "manager",
                recursionDepth = 2) }),
        @FetchGroup(name = "DescFetchGroup",
                attributes = { @FetchAttribute(name = "description") }),

        @FetchGroup(name = "DepartmentFetchGroup",
                attributes = { @FetchAttribute(name = "dept") }),

        @FetchGroup(name = "AggregateEmployeeFetchGroup1", attributes = {
                @FetchAttribute(name = "dept"),
                @FetchAttribute(name = "address"),
                @FetchAttribute(name = "manager", recursionDepth = 1) }),
        @FetchGroup(name = "AggregateEmployeeFetchGroup2",
                fetchGroups = { "AggregateEmployeeFetchGroup1" }),
        @FetchGroup(name = "AggregateEmployeeFetchGroup3", fetchGroups = {
                "DepartmentFetchGroup", "AddressFetchGroup",
                "ManagerFetchGroup1A" }),
        @FetchGroup(name = "AggregateEmployeeFetchGroup4", attributes = {
                @FetchAttribute(name = "dept"),
                @FetchAttribute(name = "address") },
                fetchGroups = { "ManagerFetchGroup1A" }) })
public class FGEmployee {
    @Id
    private int id;

    private String lastName;

    private String firstName;

    @Basic(fetch = FetchType.LAZY)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private FGDepartment dept;

    @OneToOne(fetch = FetchType.LAZY)
    private FGAddress address;

    @ManyToOne(fetch = FetchType.LAZY)
    private FGManager manager;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("AddressFetchGroup")
    private String rating;

    public FGEmployee() {

    }

    public FGEmployee(int id, String firstName, String lastName, String desc,
            FGDepartment dept, FGAddress address, FGManager manager,
            String rating) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.description = desc;
        this.dept = dept;
        this.address = address;
        this.manager = manager;
        this.rating = rating;
    }

    public FGAddress getAddress() {
        return address;
    }

    public void setAddress(FGAddress address) {
        this.address = address;
    }

    public FGDepartment getDept() {
        return dept;
    }

    public void setDept(FGDepartment dept) {
        this.dept = dept;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public FGManager getManager() {
        return manager;
    }

    public void setManager(FGManager manager) {
        this.manager = manager;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String toString() {
        return new String(this.getClass().getSimpleName() + "(id=" + this.id
                + ")");
    }

}
