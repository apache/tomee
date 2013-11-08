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


import java.util.*;

import javax.persistence.*;

import org.apache.openjpa.jdbc.meta.strats.*;
import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.jdbc.*;


@Entity
@DataStoreId(strategy = GenerationType.SEQUENCE, generator = "system")
@DataCache(enabled = false)
@FetchGroup(name = "detail", attributes = {
    @FetchAttribute(name = "rel", recursionDepth = -1),
    @FetchAttribute(name = "seq")
})
@VersionStrategy(StateComparisonVersionStrategy.ALIAS)
public class ExtensionsEntity {

    @GeneratedValue(generator = "uuid-hex")
    @Column(name = "UUID_HEX")
    private String uuid;
    
    @GeneratedValue(generator = "uuid-string")
    @Column(name = "UUID_STRING")
    private String uuidString;

    @GeneratedValue(generator = "uuid-type4-hex")
    @Column(name = "UUIDT4_HEX")
    private String uuidT4Hex;

    @GeneratedValue(generator = "uuid-type4-string")
    @Column(name = "UUIDT4_STRING")
    private String uuidT4String;

    @Basic(fetch = FetchType.LAZY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system")
    @ReadOnly
    private int seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REL_ID", referencedColumnName = "ID")
    @Dependent
    @InverseLogical("owner")
    private ExtensionsEntity rel;

    @ManyToOne
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "ID")
    private ExtensionsEntity owner;

    @ManyToMany
    @LRS
    private Collection<ExtensionsEntity> lrs;

    @ManyToMany
    @EagerFetchMode(FetchMode.JOIN)
    @ElementClassCriteria
    @ElementDependent
    private Collection<ExtensionsEntity> eager;

    @ExternalValues({ "M=1", "F=2" })
    @Type(int.class)
    private char externalValues;

    @Persistent
    @Externalizer("getName")
    @Factory("Class.forName")
    private Class externalizer;

    public char getExternalValues() {
        return this.externalValues;
    }

    public void setExternalValues(char externalValues) {
        this.externalValues = externalValues;
    }

    public Class getExternalizer() {
        return this.externalizer;
    }

    public void setExternalizer(Class externalizer) {
        this.externalizer = externalizer;
    }
}
