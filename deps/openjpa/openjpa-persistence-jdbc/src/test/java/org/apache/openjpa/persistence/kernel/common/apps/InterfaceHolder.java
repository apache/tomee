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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "holder")
public class InterfaceHolder implements Serializable {

    private Set intfs = new HashSet();

    @Basic
    @Column(length = 35)
    private String stringField;

    @OneToOne(fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private InterfaceTest intf;

    @Id
    private int id;

    public InterfaceHolder() {
    }

    public InterfaceHolder(int id) {
        this.id = id;
    }

    public void setIntf(InterfaceTest intf) {
        this.intf = intf;
    }

    public InterfaceTest getIntf() {
        return this.intf;
    }

    public void setIntfs(Set intfs) {
        this.intfs = intfs;
    }

    public Set getIntfs() {
        return this.intfs;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String toString() {
        return "intfs: " + intfs + ", StringField: " + stringField +
            ", Intf: " + intf + ".";
    }
}
