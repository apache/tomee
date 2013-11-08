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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexC
    extends ComplexB {

    private String stringC;
    private int intC;
    @Temporal(TemporalType.DATE)
    private Date dateC;
    private Set ds = new HashSet();
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexB b;

    public ComplexC() {

    }

    public ComplexC(String stringC, int intC, Date dateC, ComplexD[] ds,
        ComplexB b) {
        this.stringC = stringC;
        this.intC = intC;
        this.dateC = dateC;
        if (ds != null)
            this.ds.addAll(Arrays.asList(ds));
        this.b = b;
    }

    public void setStringC(String stringC) {
        this.stringC = stringC;
    }

    public String getStringC() {
        return this.stringC;
    }

    public void setIntC(int intC) {
        this.intC = intC;
    }

    public int getIntC() {
        return this.intC;
    }

    public void setDateC(Date dateC) {
        this.dateC = dateC;
    }

    public Date getDateC() {
        return this.dateC;
    }

    public void setDs(Set ds) {
        this.ds = ds;
    }

    public Set getDs() {
        return this.ds;
    }

    public void setB(ComplexB b) {
        this.b = b;
    }

    public ComplexB getB() {
        return this.b;
    }
}

