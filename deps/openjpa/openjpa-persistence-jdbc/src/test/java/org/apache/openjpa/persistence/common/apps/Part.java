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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name="MPTZZS")
public class Part implements Serializable {

    @Column(name="NA_PT", length=20)
    private String partName;

    @EmbeddedId
    private PartPK id = new PartPK();

    @ManyToOne
    @MapsId("textileId")
    Shirt shirt;

    public PartPK getId() {
        return id;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String aPartName) {
        partName = aPartName;
    }

    public Shirt getShirt() {
        return shirt;
    }

    public void setShirt(Shirt shirt) {
        this.shirt = shirt;
    }
}
