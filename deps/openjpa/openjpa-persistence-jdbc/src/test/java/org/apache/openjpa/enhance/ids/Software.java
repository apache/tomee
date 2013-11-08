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
package org.apache.openjpa.enhance.ids;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name="ID_SWTBL")
@IdClass(SoftwareId.class)
public class Software {
        
    @Id
    private Integer idInteger;
    
    @Id
    private int idInt;
    
    @Id
    private String idString;

    public Software() {
        
    }
    
    public void setIdInteger(Integer idInteger) {
        this.idInteger = idInteger;
    }

    public Integer getIdInteger() {
        return idInteger;
    }

    public void setIdInt(int idInt) {
        this.idInt = idInt;
    }

    public int getIdInt() {
        return idInt;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getIdString() {
        return idString;
    }

}
