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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * <p>Embedded type used in testing.</p>
 *
 * @author Abe White
 */
@Entity
//@DiscriminatorValue("cep")
public class ComplexEmbeddedPC //extends RecursivelyEmbeddedPC
{

    @Basic
    @Column(name = "cembedstring")
    private String stringField;

    @OneToOne
    private EmbeddedOwnerPC ownerField;

    private Set<String> stringSet = new HashSet<String>();

    public String getStringField() {
        return this.stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public EmbeddedOwnerPC getOwnerField() {
        return this.ownerField;
    }

    public void setOwnerField(EmbeddedOwnerPC ownerField) {
        this.ownerField = ownerField;
    }

    public Set getStringSet() {
        return this.stringSet;
    }

    public void setStringSet(Set stringSet) {
        this.stringSet = stringSet;
    }
}
