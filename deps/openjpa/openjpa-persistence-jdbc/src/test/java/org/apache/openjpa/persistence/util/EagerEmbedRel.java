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
package org.apache.openjpa.persistence.util;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Embeddable
public class EagerEmbedRel {
    
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<Integer> intVals;
    
    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private Set<EagerEntity> eagerEnts;

    public void setIntVals(Set<Integer> intVals) {
        this.intVals = intVals;
    }

    public Set<Integer> getIntVals() {
        return intVals;
    }

    
    public void setEagerEnts(Set<EagerEntity> eagerEnts) {
        this.eagerEnts = eagerEnts;
    }

    public Set<EagerEntity> getEagerEnts() {
        return eagerEnts;
    }
}
