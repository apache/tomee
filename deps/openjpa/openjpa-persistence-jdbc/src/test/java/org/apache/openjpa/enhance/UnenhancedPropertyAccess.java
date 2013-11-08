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
package org.apache.openjpa.enhance;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Version;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Basic;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.FetchType;

@Entity
@Table(name="UN_PROP")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UnenhancedPropertyAccess
    implements UnenhancedType, Serializable, Cloneable {

    private int id;
    private int version;
    private String sf = "foo";
    private String lazyField = "lazy";

    @Id @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Version
    protected int getVersion() {
        return version;
    }

    protected void setVersion(int v) {
        version = v;
    }

    @Basic
    public String getStringField() {
        return sf;
    }

    public void setStringField(String s) {
        sf = s;
    }

    @Basic(fetch = FetchType.LAZY)
    public String getLazyField() {
        return lazyField;
    }

    public void setLazyField(String s) {
        lazyField = s;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!getClass().isAssignableFrom(o.getClass()))
            return false;

        return getId() == ((UnenhancedPropertyAccess) o).getId();
    }

    public int hashCode() {
        return getId();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
