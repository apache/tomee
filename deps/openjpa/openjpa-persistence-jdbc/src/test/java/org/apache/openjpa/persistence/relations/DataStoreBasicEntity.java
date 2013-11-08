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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.apache.openjpa.persistence.DataStoreId;

@Entity
@DataStoreId
public class DataStoreBasicEntity {

    private String name;

    @ManyToOne
    private BasicEntity rel;

    @Version
    private Integer optLock;

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public BasicEntity getRel() { 
        return rel; 
    }

    public void setRel(BasicEntity rel) { 
        this.rel = rel; 
    }
}
