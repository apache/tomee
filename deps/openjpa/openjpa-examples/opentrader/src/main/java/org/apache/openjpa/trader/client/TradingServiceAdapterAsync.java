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
package org.apache.openjpa.trader.client;

import java.sql.Timestamp;
import java.util.List;

import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The asynchronous counterpart of the interface.
 * The equivalence between this asynchronous interface and its {@link TradingServiceAdapter 
 * synchronous version} is validated during GWT compilation.
 * 
 * @author Pinaki Poddar
 *
 */
public interface TradingServiceAdapterAsync {

    void ask(Trader trader, Stock stock, int volume, double price, AsyncCallback<Ask> callback);

    void bid(Trader trader, Stock stock, int volume, double price, AsyncCallback<Bid> callback);

    void getStock(String symbol, AsyncCallback<Stock> callback);

    void getStocks(AsyncCallback<List<Stock>> callback);

    void getTrades(Timestamp from, Timestamp to, AsyncCallback<List<Trade>> callback);

    void getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to,
            AsyncCallback<List<Trade>> callback);

    void login(String trader, AsyncCallback<Trader> callback);

    void matchAsk(Ask ask, AsyncCallback<List<Match>> callback);

    void matchBid(Bid bid, AsyncCallback<List<Match>> callback);

    void trade(Match match, AsyncCallback<Trade> callback);
    
    void getLog(AsyncCallback<List<LogStatement>> callback);

    void withdraw(Tradable t, AsyncCallback<Tradable> callback);
    
    void refresh(Tradable t, AsyncCallback<Tradable> callback);
    
    void getServiceURI(AsyncCallback<String> callback);
}
