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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

@Entity
public class BidiParent implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(mappedBy="oneToManyParent")
    @OrderBy("name ASC")
    private List<BidiChild> oneToManyChildren = new ArrayList<BidiChild>();

    @OneToOne(fetch=FetchType.LAZY, mappedBy="oneToOneParent")
    private BidiChild oneToOneChild;

    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public BidiChild getOneToOneChild() { 
        return oneToOneChild; 
    }

    public void setOneToOneChild(BidiChild child) { 
        oneToOneChild = child; 
    }

    public List<BidiChild> getOneToManyChildren() { 
        return oneToManyChildren; 
    }
}
