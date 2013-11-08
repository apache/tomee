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
package org.apache.openjpa.persistence.compat;

import javax.persistence.*;

@Entity
public class EntityC_B1MJT {

    @Id
    @GeneratedValue
    private long cid;

    private String name;
    
    @ManyToOne
    @JoinTable(
            name="Bi1MJT_C",
            joinColumns=
              @JoinColumn(name="C_ID", referencedColumnName="CID"),
              inverseJoinColumns=
              @JoinColumn(name="Bi1MJT_ID", referencedColumnName="BID")
    )
    Bi_1ToM_JT bi1mjt;

    public long getId() { 
        return cid; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public void setBi1mjt(Bi_1ToM_JT bi1mjt) {
        this.bi1mjt = bi1mjt;
    }
    
    public Bi_1ToM_JT getBi1mjt() {
        return bi1mjt;
    }
    
}
