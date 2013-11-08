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
package org.apache.openjpa.persistence.common.apps;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import java.io.Serializable;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="MPTZZT")
public class Textile implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ID_TXE", length=4)
    private Integer textileId;

    @Column(name="NA_TXE", length=20)
    private String txeName;

    public Integer getTextileId() {
        return textileId;
    }

    public void setTextileId(Integer aTextileId) {
        textileId = aTextileId;
    }

    public String getTxeName() {
        return txeName;
    }

    public void setTxeName(String aTxeName) {
        txeName = aTxeName;
    }
}
