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
package org.apache.openjpa.meta;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.meta.C;

@Entity
@Table(name="meta_B")
public class B extends AbstractThing {
    private Set<C> cs;
    private Set<A> as;

    @OneToMany
    public Set<C> getCs() {
        return cs;
    }

    public void setCs(Set<C> cs) {
        this.cs = cs;
    }

    @OneToMany
    public Set<A> getAs() {
        return as;
    }

    public void setAs(Set<A> as) {
        this.as = as;
    }
}
