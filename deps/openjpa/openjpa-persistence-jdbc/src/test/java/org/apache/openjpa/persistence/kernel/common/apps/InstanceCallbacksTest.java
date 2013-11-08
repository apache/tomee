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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * Used in testing; should be enhanced.
 */
@Entity
@Table(name = "icbt")
public class InstanceCallbacksTest {

    public static long preDeleteInvocations = 0;
    public static long postLoadInvocations = 0;
    public static long preStoreInvocations = 0;
    public static long preClearInvocations = 0;

    public transient boolean postLoadCalled = false;
    public transient boolean preStoreCalled = false;
    public transient boolean preDeleteCalled = false;
    public transient boolean preClearCalled = false;

    public transient int preDeleteCycle = -1;
    public transient boolean flushInPreStore = false;

    // this string should never be null in jdoPostLoad
    @Column(length = 35)
    private String nonNullString = null;

    @Column(length = 35)
    private String stringField = null;
    @Basic
    private int intField = 0;
    @Basic
    private int nonDFGField = 0;

    @OneToOne(cascade = CascadeType.PERSIST)
    private RuntimeTest1 oneOne = null;

    @OneToOne(cascade = CascadeType.PERSIST)
    private InstanceCallbacksTest rel;
    private transient Object relId;

    public InstanceCallbacksTest() {
    }

    public InstanceCallbacksTest(String stringField, int intField) {
        this.stringField = stringField;
        this.intField = intField;
    }

    public void setNonNullString(String val) {
        nonNullString = val;
    }

    public String getStringField() {
        return this.stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public int getIntField() {
        return this.intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public int getNonDFGField() {
        return this.nonDFGField;
    }

    public void setNonDFGField(int nonDFGField) {
        this.nonDFGField = nonDFGField;
    }

    public RuntimeTest1 getOneOne() {
        return this.oneOne;
    }

    public void setOneOne(RuntimeTest1 oneOne) {
        this.oneOne = oneOne;
    }

    @PostLoad
    public void jdoPostLoad() {
        postLoadInvocations++;

        postLoadCalled = true;
        if (nonNullString == null)
            throw new IllegalStateException();
    }

    public void jdoPreClear() {
        preClearInvocations++;

        preClearCalled = true;
    }

    @PrePersist
    @PreUpdate
    public void jdoPreStore() {
        preStoreInvocations++;

        preStoreCalled = true;

        // ensure that whenever this object is persisted,
        // nonNullString is, in fact, not null.
        if (nonNullString == null)
            nonNullString = "** this string is not null **";

        // assign new value to relation; should get persisted
        if ("bar".equals(stringField))
            oneOne = new RuntimeTest1("jdoPreStore",
                (int) (Math.random() * Integer.MAX_VALUE));
        OpenJPAEntityManager em = OpenJPAPersistence.getEntityManager(this);
        if (em != null) {
            if (relId != null) {
                InstanceCallbacksTest rel = em.find(InstanceCallbacksTest.class,
                    relId);
                rel.setRel(this);
                rel.setIntField(8888);
            }
            if (flushInPreStore)
                em.flush();
        }
    }

    @PreRemove
    public void jdoPreDelete() {
        preDeleteInvocations++;
        preDeleteCalled = true;
        if (preDeleteCycle >= 0 && preDeleteCycle < 5) {
            preDeleteCycle++;
            OpenJPAPersistence.getEntityManager(this).remove(this);
        }
    }

    public InstanceCallbacksTest getRel() {
        return this.rel;
    }

    public void setRel(InstanceCallbacksTest rel) {
        this.rel = rel;
    }

    public Object getRelId() {
        return this.relId;
    }

    public void setRelId(Object relId) {
        this.relId = relId;
    }
}
