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
package org.apache.openjpa.persistence.delimited.identifiers;

import javax.persistence.ColumnResult;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

@SqlResultSetMapping(name="AnimalResultSet",
    entities={@EntityResult(entityClass=Animal2.class,
        fields={
        @FieldResult(name="type", column="\"animal type\""),
        @FieldResult(name="name", column="\"animal name\"")
    },
    discriminatorColumn="\"discr col\"")},
    columns={@ColumnResult(name="\"animal age\"")})
    
@Entity
@Inheritance
@DiscriminatorColumn(name="\"discr col\"", columnDefinition="VARCHAR(10)")
@Table(name="\"Animal2\"")
public class Animal2 {
    @Id
    private int id;
    
    protected String type;
    protected String name;

    public Animal2() {}
    
    public Animal2(int id) {
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
