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
package org.apache.openjpa.persistence.relations.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LrsEntityB")
public class LrsEntityB implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Integer id;

    @Column(length = 30)
    String name;

    @ManyToOne()
    LrsEntityA entitya;

    public LrsEntityB() {
        this.name = "none";
        this.entitya = null;
    }

    public LrsEntityB(String nam) {
        this.name = nam;
        this.entitya = null;
    }

    public LrsEntityB(String nam, LrsEntityA entitya) {
        this.name = nam;
        this.entitya = entitya;
        if (entitya != null)
            entitya.getEntitybs().add(this);
    }

    public LrsEntityA getEntitya() {
        return entitya;
    }

    public void setEntitya(LrsEntityA entitya) {
        this.entitya = entitya;
        if (entitya != null)
            entitya.getEntitybs().add(this);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
