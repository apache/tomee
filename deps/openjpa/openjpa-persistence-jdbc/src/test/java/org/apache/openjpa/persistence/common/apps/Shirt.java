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
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
@Table(name="MPTZZV")
public class Shirt extends Textile implements Serializable {

    @Column(name="ID_SZE", length=1)
    private String szeId;

    public String getSzeId() {
        return szeId;
    }

    public void setSzeId(String aSzeId) {
        szeId = aSzeId;
    }

    @OneToMany(cascade=CascadeType.ALL, mappedBy="shirt", fetch=FetchType.EAGER, orphanRemoval=true)
    Collection<Part> parts = new ArrayList<Part>();

    
    public Collection<Part> getParts() {
        return parts;
    }

    public void setParts(Collection<Part> parts) {
        this.parts = parts;
    }

}
