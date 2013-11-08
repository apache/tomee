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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Widget {

    @Id
    @GeneratedValue(generator="uuid-hex")
    private String id;
    
    @Basic
    private String name;
    
    @ManyToMany(mappedBy="widgets")
    private Collection<Owner> owners;

    public Widget() {        
    }

    public Widget(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOwners(Collection<Owner> owners) {
        this.owners = owners;
    }

    public Collection<Owner> getOwners() {
        return owners;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Widget) {
            Widget widget = (Widget)obj;
            return getId().equals(widget.getId()) &&
                getName().equals(widget.getName());
        }
        return false;
    }
}
