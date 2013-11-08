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
package org.apache.openjpa.persistence.jdbc.annotations;


import java.awt.*;
import java.util.*;
import java.util.List;

import javax.persistence.*;

import org.apache.openjpa.jdbc.meta.strats.*;
import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.jdbc.*;
import org.apache.openjpa.persistence.jdbc.OrderColumn;


@Entity
@Table(name = "NONSTD_ENTITY")
@DataStoreIdColumn(name = "OID")
@DiscriminatorStrategy(ClassNameDiscriminatorStrategy.ALIAS)
@DiscriminatorColumn(name = "DISCRIM", length = 128)
@XMappingOverride(name = "superCollection",
    containerTable = @ContainerTable(name = "SUP_COLL",
        joinColumns = @XJoinColumn(name = "OWNER")),
    elementColumns = @ElementColumn(name = "SUP_ELEM"))
public class NonstandardMappingEntity
    extends NonstandardMappingMappedSuper {

    @Persistent(fetch = FetchType.LAZY)
    @Strategy("org.apache.openjpa.persistence.jdbc.annotations.PointHandler")
    @Columns({
    @Column(name = "X_COL"),
    @Column(name = "Y_COL")
        })
    @Index(name = "PNT_IDX")
    private Point custom;

    @PersistentCollection(elementType = String.class)
    @ContainerTable(name = "STRINGS_COLL",
        joinColumns = @XJoinColumn(name = "OWNER"),
        joinIndex = @Index(enabled = false))
    @ElementColumn(name = "STR_ELEM", length = 127)
    @OrderColumn(name = "ORDER_COL")
    @ElementIndex
    private List stringCollection = new ArrayList();

    @PersistentCollection
    @ContainerTable(name = "JOIN_COLL",
        joinColumns = @XJoinColumn(name = "OWNER"),
        joinForeignKey = @ForeignKey)
    @ElementJoinColumn(name = "JOIN_ELEM")
    @ElementForeignKey
    @ElementNonpolymorphic(NonpolymorphicType.JOINABLE)
    private List<NonstandardMappingEntity> joinCollection =
        new ArrayList<NonstandardMappingEntity>();

    @PersistentMap(keyType = String.class, elementType = String.class)
    @ContainerTable(name = "STRINGS_MAP",
        joinColumns = @XJoinColumn(name = "OWNER"),
        joinIndex = @Index(enabled = false))
    @KeyColumn(name = "STR_KEY", length = 127)
    @ElementColumn(name = "STR_VAL", length = 127)
    @KeyIndex
    @ElementIndex
    private Map stringMap = new HashMap();

    @PersistentMap
    @ContainerTable(name = "JOIN_MAP",
        joinColumns = @XJoinColumn(name = "OWNER"),
        joinForeignKey = @ForeignKey)
    @KeyJoinColumn(name = "JOIN_KEY")
    @KeyForeignKey
    @KeyNonpolymorphic
    @ElementJoinColumn(name = "JOIN_VAL")
    @ElementForeignKey
    @ElementNonpolymorphic
    private Map<NonstandardMappingEntity, NonstandardMappingEntity> joinMap =
        new HashMap<NonstandardMappingEntity, NonstandardMappingEntity>();

    @Embedded
    @EmbeddedMapping(nullIndicatorAttributeName = "uuid", overrides = {
    @MappingOverride(name = "rel",
        joinColumns = @XJoinColumn(name = "EM_REL_ID")),
    @MappingOverride(name = "eager",
        containerTable = @ContainerTable(name = "EM_EAGER"),
        elementJoinColumns = @ElementJoinColumn(name = "ELEM_EAGER_ID"))
        })
    private ExtensionsEntity embed;

    @PersistentCollection(elementEmbedded = true)
    @ContainerTable(name = "EMBED_COLL")
    @ElementEmbeddedMapping(overrides = {
    @XMappingOverride(name = "basic", columns = @Column(name = "EM_BASIC"))
        })
    private List<EmbedValue2> embedCollection = new ArrayList<EmbedValue2>();

    public Point getCustom() {
        return this.custom;
    }

    public void setCustom(Point custom) {
        this.custom = custom;
    }

    public List getStringCollection() {
        return this.stringCollection;
    }

    public List<NonstandardMappingEntity> getJoinCollection() {
        return this.joinCollection;
    }

    public Map getStringMap() {
        return this.stringMap;
    }

    public Map<NonstandardMappingEntity,NonstandardMappingEntity> getJoinMap() {
        return this.joinMap;
    }

    public ExtensionsEntity getEmbed() {
        return this.embed;
    }

    public void setEmbed(ExtensionsEntity embed) {
        this.embed = embed;
    }

    public List<EmbedValue2> getEmbedCollection() {
        return this.embedCollection;
    }
}
