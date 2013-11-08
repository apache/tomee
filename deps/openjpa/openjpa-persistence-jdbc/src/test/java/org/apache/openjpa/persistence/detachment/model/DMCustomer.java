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
package org.apache.openjpa.persistence.detachment.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity 
public class DMCustomer {

    private static long idCounter = System.currentTimeMillis();	
    @Id private long id = idCounter++;    
    private String firstName;    
    private String lastName;    

    @Version int version;
    
    @OneToMany(mappedBy="customer", 
            fetch=FetchType.EAGER,
            cascade=CascadeType.ALL)
            private List<DMCustomerInventory> customerInventories =
                new ArrayList<DMCustomerInventory>();

    @Temporal(TemporalType.TIMESTAMP)
    Calendar cal;
    
    public DMCustomer() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<DMCustomerInventory> getCustomerInventories() {
        return customerInventories;
    }

    public void setCustomerInventories(
            List<DMCustomerInventory> customerInventories) {
        this.customerInventories = customerInventories;
    }
    public void setCal(Calendar c){
        cal = c;
    }
    public Calendar getCal(){
        return cal;
    }
}
