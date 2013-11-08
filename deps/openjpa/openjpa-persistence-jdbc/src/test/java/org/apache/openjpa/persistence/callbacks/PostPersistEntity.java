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
package org.apache.openjpa.persistence.callbacks;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;

/**
 * A persistent entity to test that when PostPersist callback is invoked can
 * be configured. Also the auto-generated persistent identity is assigned when 
 * PostPersist is invoked.
 *  
 * @author Pinaki Poddar
 *
 */
@Entity
public class PostPersistEntity {
    @Id
    @GeneratedValue
    private long id;
    
    private String name;
    
    transient int postPersistCallbackCount;
    transient long idOnCallback;
    
    @PostPersist
    public void postPersist() {
        postPersistCallbackCount++;
        idOnCallback = ((Long)getByReflection("id")).longValue();
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
    
    /**
     * Gets value of the named field by reflection to ensure that the field is
     * not loaded as a side-effect to access it.
     * @return
     */
    Object getByReflection(String f) {
        try {
            return getClass().getDeclaredField(f).get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
}
