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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public  class BiOrderEntity implements java.io.Serializable {

    private static final long serialVersionUID = -1059986449941927485L;

    @Id
    private int id;

    private String name;

    @ManyToOne
    private BiOrderMappedByEntity bo2mbEntity;
        
    public BiOrderEntity() {
    }

    public BiOrderEntity(String name) {
        this.id = name.charAt(0) - 'A' + 1;
        this.name = name;
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

    public BiOrderMappedByEntity getEntity() {
        return bo2mbEntity;
    }

    public void setEntity(BiOrderMappedByEntity ent) {
        this.bo2mbEntity = ent;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof BiOrderEntity) {
            BiOrderEntity boe = (BiOrderEntity)obj;
            return boe.getId() == getId() &&
                boe.getName().equals(getName());
        }
        return false;
    }
}

