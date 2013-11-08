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
package org.apache.openjpa.persistence.cascade;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
        @NamedQuery(name = "VertexType.findByName",
                    query = "SELECT t FROM VertexType t where t.name=?1"),
        @NamedQuery(name = "VertexType.findAll",
                    query = "SELECT t FROM VertexType t") })
public class VertexType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)    
    private long oid;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    List<Vertex> instances;

    private String name;

    protected VertexType() {
        this.instances = new ArrayList<Vertex>();
    }

    public VertexType( String name ) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getOid() {
        return oid;
    }
}
