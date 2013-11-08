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
package org.apache.openjpa.persistence.query.common.apps;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "qtimeout")
@NamedNativeQueries({
@NamedNativeQuery(name = "NoHintList", 
    query = "select id from qtimeout where mod(DELAY(2,id),2)=0"),
@NamedNativeQuery(name = "NoHintSingle", 
    query = "select id from qtimeout where mod(DELAY(2,id),2)=1"),
@NamedNativeQuery(name = "Hint0msec", 
    query = "select id from qtimeout where mod(DELAY(2,id),2)=0", 
    hints = { @QueryHint(name = "javax.persistence.query.timeout", 
        value = "0") }),
@NamedNativeQuery(name = "Hint1000msec", 
    query = "select id from qtimeout where mod(DELAY(2,id),2)=0", 
    hints = { @QueryHint(name = "javax.persistence.query.timeout", 
        value = "1000") })
})
public class QTimeout implements Serializable {
    private static final long serialVersionUID = -622382368446668547L;

    @Id
    protected int id;

    @Basic
    @Column(length = 35)
    protected String stringField;

    @Version
    protected int versionField;

    public QTimeout() {
    }

    public QTimeout(int i, String s) {
        this.id = i;
        this.stringField = s;
    }

    public long getId() {
        return id;
    }

    public void setStringField(String val) {
        stringField = val;
    }

    public String getStringField() {
        return stringField;
    }

    @Override
    public String toString() {
        return ("id: " + id + " StringField: " + stringField);
    }
}
