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
package org.apache.openjpa.trader.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A stock is a typical financial instrument that is bought and sold by {@linkplain Trader}.
 * A stock is mostly an immutable entity, except its price. 
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
@Entity
public class Stock implements Serializable {
    /**
     * The primary identity of a Stock.
     * The uniqueness of the primary keys are often warranted by the persistence provider.
     * In this case, unique of a Stock symbol is ensured by an external agency.   
     */
    @Id
    private String symbol;
    
    /**
     * The name of the company represented by this financial instrument.
     */
    private String company;
    
    private Sector sector;
    
    @Column(precision=10,scale=2)
    private double price;
    
    @Column(precision=10,scale=2)
    private double lastPrice;
    
    /**
     * A no-arg constructor to comply to both GWT compiler and OpenJPA 
     * bytecode enhancer.
     */
    protected Stock() {
        
    }
    
    public Stock(String symbol, String company, Sector sector, double price) {
        super();
        this.symbol = symbol;
        this.company = company;
        this.sector = sector;
        this.price  = price;
        this.lastPrice = price;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getCompany() {
        return company;
    }
    
    public Sector getSector() {
        return sector;
    }
    
    public double getMarketPrice() {
        return price;
    }
    
    public void setMarketPrice(double newPrice) {
        this.lastPrice = this.price;
        this.price     = newPrice;
    }
    
    public double getLastPrice() {
        return lastPrice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stock other = (Stock) obj;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        return true;
    }
}
