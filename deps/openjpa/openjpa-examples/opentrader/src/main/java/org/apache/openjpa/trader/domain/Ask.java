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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * An offer to sell a financial instrument.
 * The only mutable state of an offer is its expiration.
 * But that state is also mutable only once and is not reversible.
 * 
 * @author Pinaki Poddar
 *
 */

@SuppressWarnings("serial")
@Entity
public class Ask  extends Tradable {
    /**
     * The trader who has made the offer.
     * Must never be null.
     */
    @ManyToOne(cascade={CascadeType.MERGE,CascadeType.DETACH},optional=false)
    private Trader seller;
    
    /**
     * A no-arg constructor to comply to both GWT compiler and OpenJPA 
     * bytecode enhancer.
     */
    protected Ask() {
        super();
    }
    
    /**
     * Real constructor populates the essential properties.
     * 
     * @param trader the trader who made this offer. Must not be null.
     * @param stock the underlying instrument. Must not be null.
     * @param price the offered price to sell. Must be positive.
     * @param volume the number of instruments to sell. Must be positive.
     */
    public Ask(Trader trader, Stock stock, double price, int volume) {
        super(stock, price, volume);
        if (trader == null)
            throw new NullPointerException("Can not create Ask with null trader");
        this.seller = trader;
    }

    /**
     * Gets the trader who made this offer to sell.
     * 
     * @return the trader who made this offer. Never null.
     */
    public Trader getSeller() {
        return seller;
    }
    
    public double getGain() {
        return (getPrice() - getStock().getMarketPrice())*getVolume(); 
    }
}
