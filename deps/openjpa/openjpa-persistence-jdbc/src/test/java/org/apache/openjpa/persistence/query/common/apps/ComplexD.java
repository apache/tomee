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
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexD {

    private String stringD;
    private int intD;
    @Temporal(TemporalType.DATE)
    private Date dateD;
    private Set es = new TreeSet();
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexC c;

    public ComplexD() {

    }

    public ComplexD(String stringD, int intD, Date dateD, ComplexE[] es,
        ComplexC c) {
        this.stringD = stringD;
        this.intD = intD;
        this.dateD = dateD;
        if (es != null)
            this.es.addAll(Arrays.asList(es));
        this.c = c;
    }

    public void setStringD(String stringD) {
        this.stringD = stringD;
    }

    public String getStringD() {
        return this.stringD;
    }

    public void setIntD(int intD) {
        this.intD = intD;
    }

    public int getIntD() {
        return this.intD;
    }

    public void setDateD(Date dateD) {
        this.dateD = dateD;
    }

    public Date getDateD() {
        return this.dateD;
    }

    public void setEs(Set es) {
        this.es = es;
    }

    public Set getEs() {
        return this.es;
    }

    public void setC(ComplexC c) {
        this.c = c;
    }

    public ComplexC getC() {
        return this.c;
    }
}

