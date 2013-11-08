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
package org.apache.openjpa.persistence.access;

import javax.persistence.Access;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

import static javax.persistence.AccessType.FIELD;

@Entity
@Access(value=FIELD)
@NamedQuery(name="FieldAccess.query", 
    query="SELECT fa FROM FieldAccess fa WHERE " + 
        "fa.id = :id AND fa.strField = :strVal")
public class FieldAccess {

    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;
    
    @Basic
    private String strField;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setStringField(String val) {
        this.setStrField(val);
    }

    public String getStringField() {
        return getStrField();
    }  
    
    public boolean equals(Object obj) {
        if (obj instanceof FieldAccess) {
            FieldAccess fa = (FieldAccess)obj;
            return id == fa.getId() &&
              getStrField().equals(fa.getStringField());
        }
        return false;
    }

    public void setStrField(String strField) {
        this.strField = strField;
    }

    public String getStrField() {
        return strField;
    }
}
