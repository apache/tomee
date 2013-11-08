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
package org.apache.openjpa.persistence.detach;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class IntVersionEntity implements Serializable {
    
    @Id
    private int id;
    private String name;
    
    @Version
    private int version;
    
    public int getVersion() {
        return version;
    }

    @OneToOne(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER)
    private TimestampVersionEntity e2;
    
    public IntVersionEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimestampVersionEntity getE2() {
        return e2;
    }

    public void setE2(TimestampVersionEntity e2) {
        this.e2 = e2;
    }

    public IntVersionEntity() {
    }
    
    public void printE2() {
        System.out.println("e2 - " + e2);
    }
}
