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

import java.util.Set;
import javax.persistence.Entity;

@Entity
public class AttachVersionA {

    private int pk;
    private int version;
    private String stringField;
    private String fetchA;
    private String fetchB;
    private AttachVersionA pc;
    private AttachVersionC embedded;
    private Set many;

    public AttachVersionA() {
    }

    public AttachVersionA(int pk) {
        this.pk = pk;
    }

    public AttachVersionA(int pk, String s) {
        this.pk = pk;
        stringField = s;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return pk;
    }

    public void setVersion(int v) {
        version = v;
    }

    public int getVersion() {
        return version;
    }

    public Object getVersionObject() {
        return new Integer(version);
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String s) {
        stringField = s;
    }

    public void setFetchA(String fetchA) {
        this.fetchA = fetchA;
    }

    public String getFetchA() {
        return fetchA;
    }

    public void setFetchB(String fetchB) {
        this.fetchB = fetchB;
    }

    public String getFetchB() {
        return fetchB;
    }

    public void setPC(AttachVersionA pc) {
        this.pc = pc;
    }

    public AttachVersionA getPC() {
        return pc;
    }

    public void setMany(Set many) {
        this.many = many;
    }

    public Set getMany() {
        return many;
    }

    public void setEmbedded(AttachVersionC embedded) {
        this.embedded = embedded;
    }

    public AttachVersionC getEmbedded() {
        return embedded;
    }
}
