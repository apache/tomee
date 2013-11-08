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
package org.apache.openjpa.persistence.fields;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
//@DetachedState(enabled = false) // ##### shouldn't need this
public class EnumFieldType {
    @Id
    @GeneratedValue
    private int intField;

    private SampleEnum enumField;

    @PersistentCollection
    private List<SampleEnum> enumList = new ArrayList<SampleEnum>();

    // for OpenJPA
    protected EnumFieldType() {
    }

    public EnumFieldType(int intField, SampleEnum enumField) {
        this.intField = intField;
        this.enumField = enumField;
    }

    public void setEnumField(SampleEnum enumField) {
        this.enumField = enumField;
    }

    public SampleEnum getEnumField() {
        return enumField;
    }

    public List<SampleEnum> getEnumList() {
        return enumList;
    }
}
