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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

@Entity
public class BiOrderMappedByEntity  {

    @Id
    private int id;

    @OneToMany(mappedBy="bo2mbEntity")
    @OrderColumn(name="bo2mEntities_ORDER")
    private List<BiOrderEntity> bo2mEntities;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public List<BiOrderEntity> getBo2mEntities() {
        return bo2mEntities;
    }

    public void setBo2mEntity(List<BiOrderEntity> names) {
        this.bo2mEntities = names;
    }

    public void addBo2mEntity(BiOrderEntity name) {
        if( bo2mEntities == null) {
            bo2mEntities = new ArrayList<BiOrderEntity>();
        }
        bo2mEntities.add(name);
    }
    
    public BiOrderEntity removeBo2mEntity(int location) {
        BiOrderEntity rtnVal = null;
        if( bo2mEntities != null) {
            rtnVal = bo2mEntities.remove(location);
        }
        return rtnVal;
    }
    
    public void insertBo2mEntity(int location, BiOrderEntity name) {
        if( bo2mEntities == null) {
            bo2mEntities = new ArrayList<BiOrderEntity>();
        }
        bo2mEntities.add(location, name);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof BiOrderMappedByEntity) {
            BiOrderMappedByEntity boe = (BiOrderMappedByEntity)obj;
            return boe.getId() == getId();
        }
        return false;
    }
}
