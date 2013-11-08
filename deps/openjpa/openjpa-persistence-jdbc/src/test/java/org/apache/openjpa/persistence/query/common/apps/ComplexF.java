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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexF
    extends ComplexE {

    private String stringF;
    private int intF;
    @Temporal(TemporalType.DATE)
    private Date dateF;
    private Collection gs = new HashSet();
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexE e;

    public ComplexF() {

    }

    public ComplexF(String stringF, int intF, Date dateF, ComplexG[] gs,
        ComplexE e) {
        this.stringF = stringF;
        this.intF = intF;
        this.dateF = dateF;
        if (gs != null)
            this.gs.addAll(Arrays.asList(gs));
        this.e = e;
    }

    public void setStringF(String stringF) {
        this.stringF = stringF;
    }

    public String getStringF() {
        return this.stringF;
    }

    public void setIntF(int intF) {
        this.intF = intF;
    }

    public int getIntF() {
        return this.intF;
    }

    public void setDateF(Date dateF) {
        this.dateF = dateF;
    }

    public Date getDateF() {
        return this.dateF;
    }

    public void setGs(Collection gs) {
        this.gs = gs;
    }

    public Collection getGs() {
        return this.gs;
    }

    public void setE(ComplexE e) {
        this.e = e;
    }

    public ComplexE getE() {
        return this.e;
    }
}

