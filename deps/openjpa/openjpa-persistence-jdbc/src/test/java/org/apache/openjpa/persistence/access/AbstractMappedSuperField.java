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
package org.apache.openjpa.persistence.access;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.AccessType;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class AbstractMappedSuperField {

    @Id
    @GeneratedValue
    private int id;
    
    @Basic
    @Access(AccessType.FIELD)
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    abstract public Date getCreateDate();
    
    abstract public void setCreateDate(Date date);    
    
    public boolean equals(Object obj) {
        if (obj instanceof AbstractMappedSuperField) {
            AbstractMappedSuperField ps = (AbstractMappedSuperField)obj;
            return id == ps.getId() &&
                   name.equals(ps.getName());
        }
        return false;
    }
}
