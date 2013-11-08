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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.util.*;
import javax.persistence.Entity;

/**
 * <p>Persistent type using a byte[] for a primary key field.  Used in
 * {@link TestByteArrayAppId}.</p>
 *
 * @author Abe White
 */
@Entity
public class ByteArrayPKPC {

    private byte[] pk = null;
    private String stringField = null;
    private List rels = new ArrayList();
    private ByteArrayPKPC parent = null;

    public ByteArrayPKPC() {
    }

    public ByteArrayPKPC(byte[] pk, String stringField) {
        this.pk = pk;
        this.stringField = stringField;
    }

    public byte[] getPK() {
        return pk;
    }

    public void setPK(byte[] pk) {
        this.pk = pk;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public ByteArrayPKPC getParent() {
        return parent;
    }

    public void setParent(ByteArrayPKPC parent) {
        this.parent = parent;
    }

    public List getRels() {
        return this.rels;
    }

    public void setRels(List rels) {
        this.rels = rels;
    }
}
