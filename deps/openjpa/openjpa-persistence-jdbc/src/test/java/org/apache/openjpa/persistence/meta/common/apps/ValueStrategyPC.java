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
package org.apache.openjpa.persistence.meta.common.apps;

import javax.persistence.Entity;

@Entity
public class ValueStrategyPC {

    private String uuid;
    private String uuidHex;
    private String name;
    private int ignoreUpdate;
    private int restrictUpdate;
    private int version;
    private int sequence;

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
}
