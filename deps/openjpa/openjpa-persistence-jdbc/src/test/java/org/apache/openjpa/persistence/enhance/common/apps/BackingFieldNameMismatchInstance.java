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
package org.apache.openjpa.persistence.enhance.common.apps;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Backing field names do not match up with the property accessor names, and
 * we use package-protected method access (which is not supported by JPA),
 *
 * @see TestPCSubclasser
 */
@Entity
@Table(name = "BACKINGMISMATCH")
public class BackingFieldNameMismatchInstance
    implements SubclassTestInstance {

    protected long _id; // protected since we don't have a setter
    private int _version;
    private String _s;

    @Id
    @GeneratedValue
    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    @Version
    public int getVersion() {
        return _version;
    }

    public void setVersion(int v) {
        _version = v;
    }

    public String getStringField() {
        return _s;
    }

    public void setStringField(String s) {
        _s = s;
    }
}
