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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "valstratpc")
public class ValueStrategyPC {

    @Id
    private int id;

    @Column(length = 35)
    private String uuid;
    @Column(length = 35)
    private String uuidHex;
    @Column(length = 35)
    private String name;
    @Column(name = "ignupdate")
    private int ignoreUpdate;
    @Column(name = "resupdate")
    private int restrictUpdate;
    private int version;
    private int sequence;

    public ValueStrategyPC() {
    }

    public ValueStrategyPC(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIgnoreUpdate() {
        return this.ignoreUpdate;
    }

    public void setIgnoreUpdate(int ignoreUpdate) {
        this.ignoreUpdate = ignoreUpdate;
    }

    public int getRestrictUpdate() {
        return this.restrictUpdate;
    }

    public void setRestrictUpdate(int restrictUpdate) {
        this.restrictUpdate = restrictUpdate;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUIDHex() {
        return this.uuidHex;
    }

    public void setUUIDHex(String uuidHex) {
        this.uuidHex = uuidHex;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSequence() {
        return this.sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuidHex() {
        return uuidHex;
    }

    public void setUuidHex(String uuidHex) {
        this.uuidHex = uuidHex;
    }
}
