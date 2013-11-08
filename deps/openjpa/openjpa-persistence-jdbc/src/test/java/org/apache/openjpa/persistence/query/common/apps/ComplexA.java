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
import java.util.LinkedList;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexA {

    private String stringA;
    private int intA;
    @Temporal(TemporalType.DATE)
    private Date dateA;
    private Collection bs = new LinkedList();

    public ComplexA() {

    }

    public ComplexA(String stringA, int intA, Date dateA, ComplexB[] bs) {
        this.stringA = stringA;
        this.intA = intA;
        this.dateA = dateA;
        if (bs != null)
            this.bs.addAll(Arrays.asList(bs));
    }

    public void setStringA(String stringA) {
        this.stringA = stringA;
    }

    public String getStringA() {
        return this.stringA;
    }

    public void setIntA(int intA) {
        this.intA = intA;
    }

    public int getIntA() {
        return this.intA;
    }

    public void setDateA(Date dateA) {
        this.dateA = dateA;
    }

    public Date getDateA() {
        return this.dateA;
    }

    public void setBs(Collection bs) {
        this.bs = bs;
    }

    public Collection getBs() {
        return this.bs;
    }
}
