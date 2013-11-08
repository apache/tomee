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

package org.apache.openjpa.enhance.stats;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Access(AccessType.PROPERTY)
public class BEntity {
    
    private int id;
    private String name;
    private boolean bool;
    
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    } 

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Access(AccessType.FIELD)
    private String desc;
    
    public String getName() {
        return name;
    }
    
    public String getCustomDesc(){
        return desc;
    }
    
    public BEntity(int id, String name, String desc) {
        super();
        setId(id);
        setName(name);
        setDesc(desc);
    }
    
    public BEntity() {
        super();
    }    
    
    public void setBool(boolean b){
        bool = b;
    }
    
    @Basic
    public boolean isBool(){
        return bool;
    }
}
