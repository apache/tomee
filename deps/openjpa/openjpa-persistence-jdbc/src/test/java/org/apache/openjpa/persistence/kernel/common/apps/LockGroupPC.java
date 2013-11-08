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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class LockGroupPC
    implements Serializable {

    @Column(name = "DEF_LockGSF")
    private String defaultLockGroupStringField;
    @Column(name = "EXDEF_LockGIF")
    private int explicitDefaultLockGroupIntField;

    @Column(name = "LockGIF")
    private int lockGroup0IntField;
    @Column(name = "LockGSF")
    private String lockGroup0StringField;

    @Column(name = "LockGRF")
    private transient RuntimeTest1 lockGroup1RelationField;
    @Column(name = "LGF")
    private int lockGroup1IntField;

    @Column(name = "UNLS")
    private String unlockedStringField;

    public void setDefaultLockGroupStringField(String val) {
        defaultLockGroupStringField = val;
    }

    public String getDefaultLockGroupStringField() {
        return defaultLockGroupStringField;
    }

    public void setExplicitDefaultLockGroupIntField(int val) {
        explicitDefaultLockGroupIntField = val;
    }

    public int getExplicitDefaultLockGroupIntField() {
        return explicitDefaultLockGroupIntField;
    }

    public void setLockGroup0IntField(int val) {
        lockGroup0IntField = val;
    }

    public int getLockGroup0IntField() {
        return lockGroup0IntField;
    }

    public void setLockGroup0StringField(String val) {
        lockGroup0StringField = val;
    }

    public String getLockGroup0StringField() {
        return lockGroup0StringField;
    }

    public void setLockGroup1RelationField(RuntimeTest1 val) {
        lockGroup1RelationField = val;
    }

    public RuntimeTest1 getLockGroup1RelationField() {
        return lockGroup1RelationField;
    }

    public void setLockGroup1IntField(int val) {
        lockGroup1IntField = val;
    }

    public int getLockGroup1IntField() {
        return lockGroup1IntField;
    }

    public void setUnlockedStringField(String val) {
        unlockedStringField = val;
    }

    public String getUnlockedStringField() {
        return unlockedStringField;
    }
}
