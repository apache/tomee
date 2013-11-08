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
package org.apache.openjpa.persistence;
import javax.persistence.*;

@Entity
@Table(name="SECURITY1")
public class Security {
    @Id
    @Column(name="SECURITY_ID")
    private long id;
    
    @Embedded
    @Column(name="SYMBOL")
    private Embed symbol;
    
    @ManyToOne(optional=false,fetch=FetchType.LAZY) 
    @JoinColumn(name="COUNTRY_ID")
    private Country country;

    @ManyToOne
    private Country countryEager;

    public Security() {
        super();
    }

    public Security(long id, Embed symbol) {
        super();
        this.id = id;
        this.symbol = symbol;
    }

    public long getId() {
        return id;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country c) {
        this.country = c;
    }

    public Country getCountryEager() {
        return countryEager;
    }

    public void setCountryEager(Country c) {
        this.countryEager = c;
    }

    public Embed getSymbol() {
        return symbol;
    }

    public void setSymbol(Embed symbol) {
        this.symbol = symbol;
    }
}

