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
package org.apache.openjpa.persistence.detachment.model;

import javax.persistence.*;

@Entity
public class SimpleC {

    @Id
    @GeneratedValue
    protected int c_id;

    @Basic
    protected String name;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name="B_ID", referencedColumnName="B_ID", nullable = false,
            updatable = false)
    protected SimpleB parent;

    public int getId() {
        return c_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(SimpleB b) {
       this.parent = b;
    }

    public SimpleB getParent() {
       return parent;
    }

}
