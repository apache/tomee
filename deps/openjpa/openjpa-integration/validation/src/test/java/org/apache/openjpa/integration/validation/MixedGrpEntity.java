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

@Entity
public class MixedGrpEntity {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    private String defNotNull;
    
    @NotNull(groups=ValGroup1.class)
    private String vg1NotNull;
    
    @NotNull(groups=ValGroup2.class)
    private String vg2NotNull;
    
    @NotNull(groups={ValGroup1.class, ValGroup2.class})
    private String vg12NotNull;

    public void setDefNotNull(String defNotNull) {
        this.defNotNull = defNotNull;
    }

    public String getDefNotNull() {
        return defNotNull;
    }

    public void setVg1NotNull(String vg1NotNull) {
        this.vg1NotNull = vg1NotNull;
    }

    public String getVg1NotNull() {
        return vg1NotNull;
    }

    public void setVg2NotNull(String vg2NotNull) {
        this.vg2NotNull = vg2NotNull;
    }

    public String getVg2NotNull() {
        return vg2NotNull;
    }

    public void setVg12NotNull(String vg12NotNull) {
        this.vg12NotNull = vg12NotNull;
    }

    public String getVg12NotNull() {
        return vg12NotNull;
    }
}
