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
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * <p>Persitent type used in testing.</p>
 *
 * @author Abe White
 */
@Entity
@Table(name = "rtest1")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RuntimeTest1 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Temporal(TemporalType.DATE)
    private Date dateField;

    @Transient
    public static final String someStaticField = "someField";

    private Locale localeField;

    @Id
    private int intField;

    private int intField1;

    @Column(length = 35)
    private String stringField;

    // transactional only
    @Column(length = 35)
    public String transString;

    // relations
    //@Transient
    @OneToOne(fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private RuntimeTest1 selfOneOne;

    @OneToMany(mappedBy = "selfOneOne",
        cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private Set<RuntimeTest1> selfOneMany = new HashSet<RuntimeTest1>();

    @Version
    private int version;

    public RuntimeTest1() {
    }

    public RuntimeTest1(int key) {
        this.intField = key;
    }

    public RuntimeTest1(String str, int i) {
        stringField = str;
        intField = i;
        intField1 = i;
    }

    public int getIntField() {
        return this.intField;
    }

    public int getIntField1() {
        return this.intField1;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public void setIntField1(int intField1) {
        this.intField1 = intField1;
    }

    public String getStringField() {
        return this.stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public RuntimeTest1 getSelfOneOne() {
        return this.selfOneOne;
    }

    public void setSelfOneOne(RuntimeTest1 selfOneOne) {
        this.selfOneOne = selfOneOne;
    }

    public Set getSelfOneMany() {
        return this.selfOneMany;
    }

    public void setSelfOneMany(Set selfOneMany) {
        this.selfOneMany = selfOneMany;
    }

    public String toString() {
        return "IntField: " + intField + ", StringField: " + stringField + " .";
    }

    public Locale getLocaleField() {
        return localeField;
    }

    public void setLocaleField(Locale localeField) {
        this.localeField = localeField;
    }

    public Date getDateField() {
        return this.dateField;
    }

    public void setDateField(Date d) {
        this.dateField = d;
    }
}
