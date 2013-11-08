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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexB
    extends ComplexA {

    private String stringB;
    private int intB;
    @Temporal(TemporalType.DATE)
    private Date dateB;
    private Collection cs = new ArrayList();
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexA a;

    public ComplexB() {

    }

    public ComplexB(String stringB, int intB, Date dateB, ComplexC[] cs,
        ComplexA a) {
        this.stringB = stringB;
        this.intB = intB;
        this.dateB = dateB;
        if (cs != null)
            this.cs.addAll(Arrays.asList(cs));
        this.a = a;
    }

    public void setStringB(String stringB) {
        this.stringB = stringB;
    }

    public String getStringB() {
        return this.stringB;
    }

    public void setIntB(int intB) {
        this.intB = intB;
    }

    public int getIntB() {
        return this.intB;
    }

    public void setDateB(Date dateB) {
        this.dateB = dateB;
    }

    public Date getDateB() {
        return this.dateB;
    }

    public void setCs(Collection cs) {
        this.cs = cs;
    }

    public Collection getCs() {
        return this.cs;
    }

    public void setA(ComplexA a) {
        this.a = a;
    }

    public ComplexA getA() {
        return this.a;
    }
}

