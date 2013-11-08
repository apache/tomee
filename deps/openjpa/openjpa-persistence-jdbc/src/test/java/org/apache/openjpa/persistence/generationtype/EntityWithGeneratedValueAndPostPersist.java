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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.*;

@Entity
public class EntityWithGeneratedValueAndPostPersist {
    @Id
    private long id;
    
    @Basic
    @GeneratedValue
    private int bingo;
    
    @Basic
    private String name;
    
    @Transient
    private ValueCache cache;
    
    public EntityWithGeneratedValueAndPostPersist(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getId() {
        return id;
    }
    
    public int getBingo() {
        return bingo;
    }
    
    public void setCache(ValueCache cache) {
        this.cache = cache;
    }
    
    @PostPersist
    private void postPersistCallback() {
        if (cache == null)
            throw new IllegalStateException("Expected a cache");
        
        cache.setValue(bingo);
    }
}
