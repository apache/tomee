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
package org.apache.openjpa.persistence.criteria.results;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A simple class for testing select projections.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class Bar {
    @Id
    private long bid;
    private short bshort;
    private String bstring;
    private int bint;
    
    protected Bar() {
        
    }
    
    public Bar(long bid, String bstring) {
        super();
        this.bid = bid;
        this.bstring = bstring;
    }
    public long getBid() {
        return bid;
    }
    public void setBid(long bid) {
        this.bid = bid;
    }
    public short getBshort() {
        return bshort;
    }
    public void setBshort(short bshort) {
        this.bshort = bshort;
    }
    public String getBstring() {
        return bstring;
    }
    public void setBstring(String bstring) {
        this.bstring = bstring;
    }
    public int getBint() {
        return bint;
    }
    public void setBint(int bint) {
        this.bint = bint;
    }
    
}
