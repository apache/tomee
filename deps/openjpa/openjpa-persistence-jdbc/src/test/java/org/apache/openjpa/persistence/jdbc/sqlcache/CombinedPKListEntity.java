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

package org.apache.openjpa.persistence.jdbc.sqlcache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

@Entity
public class CombinedPKListEntity {

    @Id
    private int id;

    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "keyA", referencedColumnName = "keyA"),
        @JoinColumn(name = "keyB", referencedColumnName = "keyB"),
        @JoinColumn(name = "keyC", referencedColumnName = "keyC") })
    private CombinedPKTestEntity te;

    @Column(insertable = false, updatable = false)
    private int keyA;
    @Column(insertable = false, updatable = false)
    private int keyB;
    @Column(insertable = false, updatable = false)
    private int keyC;

    public int getKeyA() {
        return keyA;
    }

    public void setKeyA(int keyA) {
        this.keyA = keyA;
    }

    public int getKeyB() {
        return keyB;
    }

    public void setKeyB(int keyB) {
        this.keyB = keyB;
    }

    public int getKeyC() {
        return keyC;
    }

    public void setKeyC(int keyC) {
        this.keyC = keyC;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CombinedPKTestEntity getTe() {
        return te;
    }

    public void setTe(CombinedPKTestEntity te) {
        this.te = te;
    }

}
