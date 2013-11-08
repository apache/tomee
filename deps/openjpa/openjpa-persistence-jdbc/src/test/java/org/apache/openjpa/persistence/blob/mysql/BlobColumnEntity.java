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
package org.apache.openjpa.persistence.blob.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class BlobColumnEntity {
    @Id
    private int id;

    @Lob
    @Column(length = 20)
    protected byte[] smallLob;

    @Lob
    @Column(length = 300)
    protected byte[] oldLob;
    
    @Lob
    @Column(length = 66000)
    protected byte[] medLob;
    
    @Lob
    @Column(length = 16777216)
    protected byte[] longLob;
    
    @Lob
    protected byte[] defaultLob;

    /**
     * Not suitable for all databases
     */
    @Lob
    @Column(columnDefinition = "BINARY(32)")
    protected byte[] definedLob;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getSmallLob() {
        return smallLob;
    }

    public void setSmallLob(byte[] smallLob) {
        this.smallLob = smallLob;
    }

    public byte[] getMedLob() {
        return medLob;
    }

    public void setMedLob(byte[] medLob) {
        this.medLob = medLob;
    }

    public byte[] getLongLob() {
        return longLob;
    }

    public void setLongLob(byte[] longLob) {
        this.longLob = longLob;
    }

    public byte[] getDefaultLob() {
        return defaultLob;
    }

    public void setDefaultLob(byte[] defaultLob) {
        this.defaultLob = defaultLob;
    }

    public byte[] getDefinedLob() {
        return definedLob;
    }

    public void setDefinedLob(byte[] definedLob) {
        this.definedLob = definedLob;
    }

}
