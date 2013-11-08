/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.enhance;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "UN_FIELD_WRAP")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UnenhancedFieldAccessPrimitiveWrapper
    implements UnenhancedType, Serializable, Cloneable {

    @Id
    @GeneratedValue
    private Integer id;
    @Version
    public int version;
    protected String stringField = "foo";

    @Basic(fetch = FetchType.LAZY)
    private String lazyField = "lazy";

    public int getId() {
        return id == null ? -1 : id;
    }

    public void setStringField(String s) {
        stringField = s;
    }

    public String getStringField() {
        return stringField;
    }

    public String getLazyField() {
        return lazyField;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!getClass().isAssignableFrom(o.getClass()))
            return false;

        return id == ((UnenhancedFieldAccessPrimitiveWrapper) o).id;
    }

    public int hashCode() {
        return id == null ? 0 : id;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
