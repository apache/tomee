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
package org.apache.openjpa.persistence.jdbc.annotations;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name = "NONSTD_ENTITY3")
public class NonstandardMappingEntity3 {
    @Id
    private long id;
    
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="EmbedVal3s")
    private List<EmbedValue3> embedVal3s = new ArrayList<EmbedValue3>();

    @Embedded
    private EmbedValue3 embedVal3;

    public long getId() { 
        return id; 
    }
    
    public void setId(long id) { 
        this.id = id; 
    }
   
    public List<EmbedValue3> getEmbedVal3s() { 
        return embedVal3s; 
    }
    
    public void setEmbedVal3s(List<EmbedValue3> embedVal3s) { 
        this.embedVal3s = embedVal3s; 
    }

    public EmbedValue3 getEmbedVal3() { 
        return embedVal3; 
    }
    
    public void setEmbedVal3(EmbedValue3 embedVal3) { 
        this.embedVal3 = embedVal3; 
    }
}
