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


import javax.persistence.*;

@Embeddable
public class EmbedValue {

    @Basic
    @Column(name = "EMB_BASIC")
    protected String basic;

    @Lob
    @Column(name = "EMB_CLOB")
    protected String clob;

    @Lob
    @Column(name = "EMB_BLOB")
    protected byte[] blob;

    @ManyToOne
    @JoinColumn(name = "EMB_REL")
    protected EmbedOwner owner;

    @Transient
    private int transientField;

    public void setBasic(String basic) {
        this.basic = basic;
    }

    public String getBasic() {
        return basic;
    }

    public void setClob(String clob) {
        this.clob = clob;
    }

    public String getClob() {
        return clob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setOwner(EmbedOwner owner) {
        this.owner = owner;
    }

    public EmbedOwner getOwner() {
        return owner;
    }
}
