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

import javax.persistence.AttributeOverride;
import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;

@Entity
public class MultipleSameTypedEmbedded { 

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @Embedded
    @AttributeOverride(name="name", column=@Column(name="E1_NAME"))
    @AssociationOverride(name="rel", joinColumns=@JoinColumn(name="E1_REL"))
    private EmbeddableWithRelation embed1;

    @Embedded
    @AttributeOverride(name="name", column=@Column(name="E2_NAME"))
    @AssociationOverride(name="rel", joinColumns=@JoinColumn(name="E2_REL"))
    private EmbeddableWithRelation embed2;

    @Version
    private Integer optLock;

    public long getId() { 
        return id; 
    }

    public EmbeddableWithRelation getEmbed1() {
        return embed1; 
    }

    public void setEmbed1(EmbeddableWithRelation embed1) {
        this.embed1 = embed1; 
    }

    public EmbeddableWithRelation getEmbed2() {
        return embed2; 
    }

    public void setEmbed2(EmbeddableWithRelation embed2) {
        this.embed2 = embed2; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }
}
