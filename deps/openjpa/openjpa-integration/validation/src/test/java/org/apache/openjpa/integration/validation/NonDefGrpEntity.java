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
package org.apache.openjpa.integration.validation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import org.apache.openjpa.integration.validation.ValGroup1;

@Entity
public class NonDefGrpEntity {

    @Id
    @GeneratedValue
    private int id;
    
    // NotNull constraint with default validation group
    @NotNull
    private String dgName;
    
    // NotNull constraint with specified validation group
    @NotNull(groups=ValGroup1.class)
    private String ndgName;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDgName(String dgName) {
        this.dgName = dgName;
    }

    public String getDgName() {
        return dgName;
    }    

    public void setNdgName(String dgName) {
        this.ndgName = dgName;
    }

    public String getNdgName() {
        return ndgName;
    }    
}
