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
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class ManyOneIdOwner {

    @Id
    @ManyToOne
    private BasicEntity id;

    private String name;

    @ManyToOne
    private ManyOneIdOwner selfRel;

    @Version
    private Integer optLock;

    public BasicEntity getId() { 
        return id; 
    }

    public void setId(BasicEntity id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public ManyOneIdOwner getSelfRel() { 
        return selfRel; 
    }

    public void setSelfRel(ManyOneIdOwner selfRel) { 
        this.selfRel = selfRel; 
    }
}
