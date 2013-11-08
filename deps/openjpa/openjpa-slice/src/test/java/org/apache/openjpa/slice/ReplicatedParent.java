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
package org.apache.openjpa.slice;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
public class ReplicatedParent {
    @Id 
    private String name;
    
    @OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
    private Set<ReplicatedChild> children;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<ReplicatedChild> getChildren() {
        return children;
    }
    
    public void addChild(ReplicatedChild child) {
        if (children == null)
            children = new HashSet<ReplicatedChild>();
        children.add(child);
        child.setParent(this);
    }
}
