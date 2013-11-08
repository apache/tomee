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
package org.apache.openjpa.persistence.property;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="PUBPROT_TBL")
public class AccessModsEntity {

    private int id;
    
    private String pubString;
    
    private String protString;
    
    private String privString;
    
    
    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setPubString(String pubString) {
        this.pubString = pubString;
    }

    @Column(name="PUB_COL")
    public String getPubString() {
        return pubString;
    }

    protected void setProtString(String protString) {
        this.protString = protString;
    }

    @Column(name="PROT_COL")
    protected String getProtString() {
        return protString;
    }

    private void setPrivString(String privString) {
        this.privString = privString;
    }

    @Column(name="PRIV_COL")
    private String getPrivString() {
        return privString;
    }    

    // Transient public wrapper around private property
    public void setPubPrivString(String privString) {
        setPrivString(privString);
    }

    // Transient public wrapper around private property
    @Transient
    public String getPubPrivString() {
        return getPrivString();
    }    
}
