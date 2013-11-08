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
package org.apache.openjpa.persistence.relations;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;

import org.apache.openjpa.persistence.LRS;

@Entity
public class LRSEntity {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @ManyToMany
    @OrderBy("name ASC")
    @LRS
    private Collection<BasicEntity> lrsList;

    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public Collection<BasicEntity> getLRSList() {
        if (lrsList == null) {
            lrsList = new ArrayList<BasicEntity>();
        }
        return lrsList;
    }
}
