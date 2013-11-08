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
package org.apache.openjpa.persistence.embed.attrOverrides;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity 
@Table(name="PSN")
public class Person {
    @Id 
    protected String ssn;
    protected String name;

    @ElementCollection
    @OrderBy("zipcode.zip, zipcode.plusFour")
    protected List<Address> residences = new ArrayList<Address>();
    
    @ElementCollection
    @CollectionTable(name="PSN_nickNames")
    @OrderBy("DESC")
    private List<String> nickNames = new ArrayList<String>();

    public List<String> getNickNames() {
        return nickNames;
    }
    
    public void setNickNames(List<String> nickNames) {
        this.nickNames = nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }
    
    public String getSsn() {
        return ssn;
    }
    
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public List<Address> getResidences() {
        return residences;
    }
    
    public void setResidences(List<Address> residences) {
        this.residences = residences;
    }
    
    public void addResidence(Address residence) {
        residences.add(residence);
    }
}


