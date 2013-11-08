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
package
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype;

import javax.persistence.*;

@Entity
@Table(name = "EMIDENTITY")
@SqlResultSetMapping(name = "EmbeddedIdMapping", entities = {
@EntityResult(entityClass = EmbeddedIdEntity.class, fields = {
@FieldResult(name = "id.pk1", column = "OWNER_PK1"),
@FieldResult(name = "id.pk2", column = "OWNER_PK2"),
@FieldResult(name = "id.pk3", column = "OWNER_PK3"),
@FieldResult(name = "value", column = "OWNER_VAL"),
@FieldResult(name = "relation.id.pk1", column = "REL_PK1"),
@FieldResult(name = "relation.id.pk2", column = "REL_PK2"),
@FieldResult(name = "relation.id.pk3", column = "REL_PK3")
    }),
@EntityResult(entityClass = EmbeddedIdEntity.class, fields = {
@FieldResult(name = "id.pk1", column = "REL_PK1"),
@FieldResult(name = "id.pk2", column = "REL_PK2"),
@FieldResult(name = "id.pk3", column = "REL_PK3"),
@FieldResult(name = "value", column = "REL_VAL")
    })
    })
public class EmbeddedIdEntity {

    @EmbeddedId
    private EmbeddedIdClass id;

    @Column(name = "VAL")
    private String value;

    @ManyToOne
    private EmbeddedIdEntity relation;

    @ManyToOne
    @JoinColumns({
    @JoinColumn(name = "MREL_PK1", referencedColumnName = "EPK1"),
    @JoinColumn(name = "MREL_PK2", referencedColumnName = "EPK2")
        })
    private EmbeddedIdEntity mapOverrideRelation;

    public EmbeddedIdClass getId() {
        return id;
    }

    public void setId(EmbeddedIdClass id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public EmbeddedIdEntity getRelation() {
        return relation;
    }

    public void setRelation(EmbeddedIdEntity relation) {
        this.relation = relation;
    }

    public EmbeddedIdEntity getMappingOverrideRelation() {
        return mapOverrideRelation;
    }
}
