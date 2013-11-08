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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("SINGER")
public class Singer extends Person {
    
    @OneToMany
    private Set<CD> cds;

    public Singer() {
        super();
    }

    public Singer(String firstName, String lastName, short age, int yob) {
        super(firstName, lastName, age, yob);
    }
    
    public Set<CD> getCds() {
        return cds;
    }

    public void addCd(CD cd) {
        if (cds == null)
            cds = new HashSet<CD>();
        if (cds.add(cd))
            cd.setSinger(this);
    }
}
