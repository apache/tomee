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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name="PROPREC_ATTROVER")
public class PropertyRecord {
    @EmbeddedId PropertyOwner owner;
    
    @AttributeOverrides({
        @AttributeOverride(name="key.street",
                column=@Column(name="STREET_NAME")),
        @AttributeOverride(name="value.size",
                column=@Column(name="SQUARE_FEET")),
        @AttributeOverride(name="value.tax", column=@Column(name="ASSESSMENT"))
    })
    @ElementCollection
    @CollectionTable(name="PROPREC_ATTROVER_parcels")
    Map<Address, PropertyInfo> parcels = new HashMap<Address, PropertyInfo>();
    
    @Column(length = 10)
    String description;
    
    public String getDesc() {
        return description;
    }
    
    public void setDesc(String desc) {
        this.description = desc;
    }
    
    public PropertyOwner getOwner() {
    	return owner;
    }
    
    public void setOwner(PropertyOwner owner) {
    	this.owner = owner;
    }
    
    public Map<Address, PropertyInfo> getParcels() {
    	return parcels;
    }
    
    public void addParcel(Address addr, PropertyInfo p) {
    	parcels.put(addr, p);
    }
}
