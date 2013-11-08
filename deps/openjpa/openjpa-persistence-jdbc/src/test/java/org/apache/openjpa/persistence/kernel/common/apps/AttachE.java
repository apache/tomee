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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

@Entity
@DiscriminatorValue("ATTACH_E")
@FetchGroups({
@FetchGroup(name = "all", attributes = {
@FetchAttribute(name = "b"),
@FetchAttribute(name = "f")
    })
    })
@Table(name="K_ATTACHE")
public class AttachE implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "E_ID")
    private int id;

    private Object version;

    @Basic
    private String estr;

    private int eint;

    private double edbl;

    @ManyToOne(fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private AttachB b;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "e",
        cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private AttachF f;

    @Temporal(TemporalType.DATE)
    private Date dateField;

    public int getId() {
        return id;
    }

    public void setEstr(String estr) {
        this.estr = estr;
    }

    public String getEstr() {
        return this.estr;
    }

    public void setEint(int eint) {
        this.eint = eint;
    }

    public int getEint() {
        return this.eint;
    }

    public void setEdbl(double edbl) {
        this.edbl = edbl;
    }

    public double getEdbl() {
        return this.edbl;
    }

    public void setB(AttachB b) {
        this.b = b;
    }

    public AttachB getB() {
        return this.b;
    }

    public void setF(AttachF f) {
        this.f = f;
    }

    public AttachF getF() {
        return this.f;
    }

    public void setDateField(Date date) {
        this.dateField = date;
    }

    public Date getDateField() {
        return this.dateField;
    }
}
