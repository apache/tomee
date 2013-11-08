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

/**
 * A pair of matching offer to {@linkplain Bid buy} and {@linkplain Ask sell}.
 * This is <em>not</em> a persistent entity. But it is in the persistent domain 
 * because it is often is the result of query.
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class Match implements Serializable {
    private Ask ask;
    private Bid bid;
    
    /**
     * A no-arg constructor to comply to GWT compiler.
     */
    protected Match() {
    }
    
    /**
     * Constructs a pair with matching offers.
     * @param a the offer to sell. Must not be null.
     * @param b the offer to buy. Must not be null.
     */
    public Match(Ask a, Bid b) {
        if (a == null)
            throw new NullPointerException("Can not create Match with null Ask");
        if (b == null)
            throw new NullPointerException("Can not create Match with null Bid");
        if (a.getSeller().equals(b.getBuyer())) {
            throw new NullPointerException("Can not create Match with same Trader " 
                    + a.getSeller() + " for Ask and Bid");
        }
        if (a.getPrice() > b.getPrice()) {
            throw new IllegalArgumentException("Ask price " + a.getPrice() + " is greater than "
                    + " Bid price " + b.getPrice());
        }
        ask = a;
        bid = b;
    }
    
    /**
     * Gets the matching offer to sell. 
     * 
     * @return the matching offer to sell. Never null. 
     */
    public Ask getAsk() {
        return ask;
    }
    
    /**
     * Gets the matching offer to buy.
     * 
     * @return the matching offer to buy. Never null. 
     */
    public Bid getBid() {
        return bid;
    }
    
    
    public String toString() {
        return "Match ["+ ask + " and " + bid + "]";
    }
}
