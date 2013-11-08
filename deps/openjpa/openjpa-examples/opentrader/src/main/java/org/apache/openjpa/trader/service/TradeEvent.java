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
package org.apache.openjpa.trader.service;

public class TradeEvent {
    enum Type {LOGIN, ASK, BID, WITHDRAW, REFRESH, TRADE, MATCH, 
               STOCK_LOOKUP, QUERY_TRADE, MARKET_FEED};
    
    public final Type type;
    public final long time;
    public final String description;
    
    public TradeEvent(Type type, long elapsedTime) {
        this(type, elapsedTime, "");
    }
    
    public TradeEvent(Type type, long elapsedTime, String txt) {
        this.type = type;
        this.time = elapsedTime;
        this.description = txt;
    }
}
